package ru.naumen.bpms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.report.Report;
import ru.naumen.bpms.model.report.ReportStatus;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.ReportRepository;
import ru.naumen.bpms.repository.UserRepository;
import ru.naumen.bpms.service.ReportService;
import ru.naumen.bpms.service.exception.report.ReportNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final SpringTemplateEngine templateEngine;
    private final TransactionTemplate transactionTemplate;

    public ReportServiceImpl(ReportRepository reportRepository,
                             UserRepository userRepository,
                             ProcessDefinitionRepository processDefinitionRepository,
                             SpringTemplateEngine templateEngine,
                             TransactionTemplate transactionTemplate) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.processDefinitionRepository = processDefinitionRepository;
        this.templateEngine = templateEngine;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public Long createReport() {
        Report report = Report.created();
        return reportRepository.save(report).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Report getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public String getReportContent(Long id) {
        Report report = getReport(id);

        if (report.getStatus() == ReportStatus.CREATED) {
            return buildServiceMessageHtml(
                    "Отчет еще формируется",
                    "Статус отчета: CREATED. Повторите запрос позже."
            );
        }

        if (report.getStatus() == ReportStatus.ERROR) {
            return report.getContent() != null
                    ? report.getContent()
                    : buildServiceMessageHtml(
                    "Ошибка формирования отчета",
                    "Содержимое ошибки отсутствует."
            );
        }

        return report.getContent();
    }

    @Override
    public CompletableFuture<Void> generateReportAsync(Long reportId) {
        return CompletableFuture.runAsync(() -> generateReport(reportId));
    }

    private void generateReport(Long reportId) {
        long reportStartTime = System.currentTimeMillis();

        AtomicReference<Long> usersCount = new AtomicReference<>();
        AtomicReference<Long> usersElapsedTime = new AtomicReference<>();

        AtomicReference<List<ProcessDefinition>> processDefinitions = new AtomicReference<>();
        AtomicReference<Long> processDefinitionsElapsedTime = new AtomicReference<>();

        AtomicReference<Throwable> error = new AtomicReference<>();

        Thread usersThread = new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                usersCount.set(userRepository.count());

                usersElapsedTime.set(System.currentTimeMillis() - startTime);
            } catch (Throwable throwable) {
                error.compareAndSet(null, throwable);
            }
        }, "bpms-report-users-counter");

        Thread processDefinitionsThread = new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                List<ProcessDefinition> result = new ArrayList<>();
                processDefinitionRepository.findAll().forEach(result::add);

                processDefinitions.set(result);
                processDefinitionsElapsedTime.set(System.currentTimeMillis() - startTime);
            } catch (Throwable throwable) {
                error.compareAndSet(null, throwable);
            }
        }, "bpms-report-process-definitions-loader");

        usersThread.start();
        processDefinitionsThread.start();

        try {
            usersThread.join();
            processDefinitionsThread.join();

            Throwable throwable = error.get();
            if (throwable != null) {
                markReportAsError(reportId, throwable);
                return;
            }

            long totalElapsedTime = System.currentTimeMillis() - reportStartTime;

            String html = buildReportHtml(
                    usersCount.get(),
                    usersElapsedTime.get(),
                    processDefinitions.get(),
                    processDefinitionsElapsedTime.get(),
                    totalElapsedTime
            );

            markReportAsCompleted(reportId, html);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            markReportAsError(reportId, ex);
        } catch (Throwable throwable) {
            markReportAsError(reportId, throwable);
        }
    }

    private String buildReportHtml(Long usersCount,
                                   Long usersElapsedTime,
                                   List<ProcessDefinition> processDefinitions,
                                   Long processDefinitionsElapsedTime,
                                   Long totalElapsedTime) {
        Context context = new Context();

        context.setVariable("generatedAt", LocalDateTime.now());
        context.setVariable("usersCount", usersCount);
        context.setVariable("usersElapsedTime", usersElapsedTime);
        context.setVariable("processDefinitions", processDefinitions);
        context.setVariable("processDefinitionsElapsedTime", processDefinitionsElapsedTime);
        context.setVariable("totalElapsedTime", totalElapsedTime);

        return templateEngine.process("report", context);
    }

    private void markReportAsCompleted(Long reportId, String html) {
        transactionTemplate.executeWithoutResult(status -> {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            report.setStatus(ReportStatus.COMPLETED);
            report.setContent(html);

            reportRepository.save(report);
        });
    }

    private void markReportAsError(Long reportId, Throwable throwable) {
        transactionTemplate.executeWithoutResult(status -> {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            report.setStatus(ReportStatus.ERROR);
            report.setContent(buildServiceMessageHtml(
                    "Ошибка формирования отчета",
                    throwable.getClass().getSimpleName() + ": " + throwable.getMessage()
            ));

            reportRepository.save(report);
        });
    }

    private String buildServiceMessageHtml(String title, String message) {
        Context context = new Context();

        context.setVariable("title", title);
        context.setVariable("message", message);

        return templateEngine.process("message", context);
    }
}
