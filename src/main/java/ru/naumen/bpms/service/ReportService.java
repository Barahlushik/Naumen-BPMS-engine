package ru.naumen.bpms.service;


import ru.naumen.bpms.model.report.Report;

import java.util.concurrent.CompletableFuture;

public interface ReportService {

    Long createReport();

    Report getReport(Long id);

    String getReportContent(Long id);

    CompletableFuture<Void> generateReportAsync(Long reportId);
}