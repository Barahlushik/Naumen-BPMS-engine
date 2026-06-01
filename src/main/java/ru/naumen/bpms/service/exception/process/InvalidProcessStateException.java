package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class InvalidProcessStateException extends BpmsException {
    public InvalidProcessStateException(String state) {
        super("Invalid process state: " + state,
                "INVALID_PROCESS_STATE");
    }
}