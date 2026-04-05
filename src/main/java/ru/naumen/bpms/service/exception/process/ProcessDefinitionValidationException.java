package ru.naumen.bpms.service.exception.process;


import ru.naumen.bpms.service.exception.BpmsException;

public class ProcessDefinitionValidationException extends BpmsException {
    public ProcessDefinitionValidationException(String message) {
        super(message, "PROCESS_DEFINITION_VALIDATION_ERROR");
    }
}