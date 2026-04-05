package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class ProcessAlreadyCompletedException extends BpmsException {
    public ProcessAlreadyCompletedException(Long processInstanceId) {
        super("ProcessInstance " + processInstanceId + " is already completed",
                "PROCESS_ALREADY_COMPLETED");
    }
}