package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class ParticipantAlreadyExistsException extends BpmsException {
    public ParticipantAlreadyExistsException(Long userId) {
        super("User " + userId + " is already a participant",
                "PARTICIPANT_ALREADY_EXISTS");
    }
}