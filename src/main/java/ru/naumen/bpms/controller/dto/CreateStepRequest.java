package ru.naumen.bpms.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.naumen.bpms.model.StepType;

public record CreateStepRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        StepType type
) {
}

