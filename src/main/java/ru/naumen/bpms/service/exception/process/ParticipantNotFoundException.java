package ru.naumen.bpms.service.exception.process;

import ru.naumen.bpms.service.exception.BpmsException;

public class ParticipantNotFoundException extends BpmsException {
    public ParticipantNotFoundException(Long userId) {
        super("User " + userId + " is not a participant",
                "PARTICIPANT_NOT_FOUND");
    }
}