package ru.naumen.bpms.service.exception.user;

import ru.naumen.bpms.service.exception.BpmsException;

public class UserNotFoundException extends BpmsException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
