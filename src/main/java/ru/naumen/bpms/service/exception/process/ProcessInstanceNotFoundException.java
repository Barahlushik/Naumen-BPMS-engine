package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class ProcessInstanceNotFoundException extends BpmsException {
    public ProcessInstanceNotFoundException(Long id) {
        super("ProcessInstance with id=" + id + " not found",
                "PROCESS_INSTANCE_NOT_FOUND");
    }
}
