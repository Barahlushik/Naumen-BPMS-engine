package ru.naumen.bpms.service.exception.process;


import ru.naumen.bpms.service.exception.BpmsException;

public class ProcessDefinitionNotFoundException extends BpmsException {
    public ProcessDefinitionNotFoundException(String message) {
        super(message, "PROCESS_DEFINITION_NOT_FOUND");
    }
}