package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class NoCurrentStepException extends BpmsException {
    public NoCurrentStepException(Long processInstanceId) {
        super("ProcessInstance " + processInstanceId + " has no current step",
                "NO_CURRENT_STEP");
    }
}
