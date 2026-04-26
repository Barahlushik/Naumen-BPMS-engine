package ru.naumen.bpms.service.exception.report;

import ru.naumen.bpms.service.exception.BpmsException;

public class ReportNotFoundException extends BpmsException {

    public ReportNotFoundException(Long id) {
        super("Отчет с id=%d не найден.".formatted(id), "REPORT_NOT_FOUND");
    }
}