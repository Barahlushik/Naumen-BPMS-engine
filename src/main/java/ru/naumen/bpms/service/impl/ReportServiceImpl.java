package ru.naumen.bpms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        Long reportId = reportRepository.save(report).getId();
        log.info("Report created. reportId={}", reportId);
        return reportId;
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
        log.info("Report async generation requested. reportId={}", reportId);
        return CompletableFuture.runAsync(() -> generateReport(reportId));
    }

    private void generateReport(Long reportId) {
        long reportStartTime = System.currentTimeMillis();
        log.info("Report generation started. reportId={}", reportId);

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
                log.error("Report generation worker failed. reportId={}", reportId, throwable);
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
            log.info("Report generation completed. reportId={}, elapsedMs={}", reportId, totalElapsedTime);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Report generation was interrupted. reportId={}", reportId, ex);
            markReportAsError(reportId, ex);
        } catch (Throwable throwable) {
            log.error("Report generation failed. reportId={}", reportId, throwable);
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
            log.info("Report marked as completed. reportId={}", reportId);
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
            log.warn("Report marked as failed. reportId={}, errorType={}, message={}",
                    reportId, throwable.getClass().getSimpleName(), throwable.getMessage());
        });
    }

    private String buildServiceMessageHtml(String title, String message) {
        Context context = new Context();

        context.setVariable("title", title);
        context.setVariable("message", message);

        return templateEngine.process("message", context);
    }
}
