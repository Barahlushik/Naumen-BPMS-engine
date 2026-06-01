package ru.naumen.bpms.controller.dto;

import java.util.List;

public record ProcessDefinitionResponseDto(
        Long id,
        String title,
        String description,
        String category,
        List<StepDefinitionResponseDto> steps,
        List<TransitionResponseDto> transitions
) {
}
