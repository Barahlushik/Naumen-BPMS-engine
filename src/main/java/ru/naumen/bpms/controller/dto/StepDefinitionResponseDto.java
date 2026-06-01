package ru.naumen.bpms.controller.dto;


import ru.naumen.bpms.model.StepType;

public record StepDefinitionResponseDto(
        Long id,
        String name,
        StepType type
) {
}