package ru.naumen.bpms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.naumen.bpms.controller.dto.ReportInfoResponseDto;
import ru.naumen.bpms.controller.mapper.ReportMapper;
import ru.naumen.bpms.model.report.Report;
import ru.naumen.bpms.model.report.ReportStatus;
import ru.naumen.bpms.service.ReportService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Slf4j
@Tag(name = "Reports")
@SecurityRequirement(name = "sessionAuth")
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    public ReportController(ReportService reportService,
                            ReportMapper reportMapper) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
    }

    @PostMapping
    @Operation(summary = "Создать отчет и запустить асинхронное формирование")
    public ResponseEntity<Map<String, Object>> createReport() {
        log.info("API request: create report.");
        Long reportId = reportService.createReport();

        reportService.generateReportAsync(reportId);

        log.info("API response accepted: report generation started. reportId={}", reportId);
        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "reportId", reportId,
                        "status", ReportStatus.CREATED,
                        "message", "Отчет создан и поставлен на асинхронное формирование."
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить состояние отчета")
    public ResponseEntity<ReportInfoResponseDto> getReportInfo(@PathVariable Long id) {
        log.info("API request: get report info. reportId={}", id);
        var report = reportService.getReport(id);
        log.info("API response prepared: report info. reportId={}, status={}", id, report.getStatus());
        return ResponseEntity.ok(reportMapper.toInfoDto(report));
    }

    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Получить HTML-содержимое отчета")
    public ResponseEntity<String> getReportContent(@PathVariable Long id) {
        log.info("API request: get report content. reportId={}", id);
        Report report = reportService.getReport(id);
        String content = reportService.getReportContent(id);

        HttpStatus status = switch (report.getStatus()) {
            case CREATED -> HttpStatus.ACCEPTED;
            case COMPLETED -> HttpStatus.OK;
            case ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        log.info("API response prepared: report content. reportId={}, reportStatus={}, httpStatus={}, contentLength={}",
                id, report.getStatus(), status.value(), content != null ? content.length() : 0);

        return ResponseEntity
                .status(status)
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8
                )
                .body(content);
    }
}
