package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class InvalidTransitionException extends BpmsException {
    public InvalidTransitionException(Long transitionId, Long currentStepId) {
        super("Transition " + transitionId + " is not allowed from step " + currentStepId,
                "INVALID_TRANSITION");
    }
}
