package ru.naumen.bpms.service.exception.user;

import ru.naumen.bpms.service.exception.BpmsException;

public class UserAlreadyExistException extends BpmsException {

    public UserAlreadyExistException(String message) {
        super(message, "USER_ALREADY_EXIST");
    }
}
