package ru.naumen.bpms.service.exception;

public class BpmsException extends RuntimeException {
    private final String errorCode;

    public BpmsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
