package ru.naumen.bpms.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProcessDefinitionRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 500)
        String description,

        @Size(max = 60)
        String category
) {
}

