package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class TransitionNotFoundException extends BpmsException {
    public TransitionNotFoundException(String message) {
        super(message, "TRANSITION_NOT_FOUND");
    }
}