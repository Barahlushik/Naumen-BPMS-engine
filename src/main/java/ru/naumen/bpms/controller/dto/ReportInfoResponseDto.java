package ru.naumen.bpms.controller.dto;

import ru.naumen.bpms.model.report.ReportStatus;

public record ReportInfoResponseDto(
        Long id,
        ReportStatus status,
        boolean contentReady
) {
}