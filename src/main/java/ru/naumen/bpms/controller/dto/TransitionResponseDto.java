package ru.naumen.bpms.controller.dto;

public record TransitionResponseDto(
        Long id,
        String name,
        String condition,
        Long fromStepId,
        String fromStepName,
        Long toStepId,
        String toStepName,
        Long processDefinitionId
) {
}