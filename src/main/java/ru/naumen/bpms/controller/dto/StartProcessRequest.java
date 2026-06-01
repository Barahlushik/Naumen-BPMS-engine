package ru.naumen.bpms.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartProcessRequest(
        @NotNull
        @Positive
        Long processDefinitionId,

        @NotNull
        @Positive
        Long startStepId
) {
}

