package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class TransitionValidationException extends BpmsException {

    public TransitionValidationException(String message) {
        super(message, "TRANSITION_VALIDATION_ERROR");
    }

}