package ru.naumen.bpms.controller.dto;

import ru.naumen.bpms.model.ProcessStatus;

import java.time.LocalDateTime;

public record ProcessInstanceResponseDto(
        Long id,
        Long processDefinitionId,
        Long ownerId,
        Long currentStepId,
        ProcessStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}

