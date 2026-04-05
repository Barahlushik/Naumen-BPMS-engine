package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class StepNotFoundException extends BpmsException {
    public StepNotFoundException(String message) {
        super(message, "STEP_NOT_FOUND");
    }
}