package ru.naumen.bpms.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.naumen.bpms.model.report.Report;
import ru.naumen.bpms.model.report.ReportStatus;
import ru.naumen.bpms.service.ReportService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport() {
        Long reportId = reportService.createReport();

        reportService.generateReportAsync(reportId);

        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "reportId", reportId,
                        "status", ReportStatus.CREATED,
                        "message", "Отчет создан и поставлен на асинхронное формирование."
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReportInfo(@PathVariable Long id) {
        Report report = reportService.getReport(id);

        return ResponseEntity.ok(Map.of(
                "id", report.getId(),
                "status", report.getStatus(),
                "contentReady", report.getStatus() == ReportStatus.COMPLETED
        ));
    }

    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getReportContent(@PathVariable Long id) {
        Report report = reportService.getReport(id);
        String content = reportService.getReportContent(id);

        HttpStatus status = switch (report.getStatus()) {
            case CREATED -> HttpStatus.ACCEPTED;
            case COMPLETED -> HttpStatus.OK;
            case ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity
                .status(status)
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8
                )
                .body(content);
    }
}