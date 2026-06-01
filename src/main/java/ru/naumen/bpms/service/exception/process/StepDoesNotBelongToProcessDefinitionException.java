package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class StepDoesNotBelongToProcessDefinitionException extends BpmsException {

    public StepDoesNotBelongToProcessDefinitionException(String message) {
        super(message, "STEP_VALIDATION_ERROR");
    }

}
