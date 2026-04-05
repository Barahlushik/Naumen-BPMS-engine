package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class ProcessInstanceException extends BpmsException {

    public ProcessInstanceException(String message) {
        super(message, "PROCESS_INSTANCE_ERROR");
    }
}
