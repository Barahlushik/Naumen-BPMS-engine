package ru.naumen.bpms.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.naumen.bpms.controller.dto.ProcessValidationResponseDto;

public interface ProcessDefinitionValidationService {

    ProcessValidationResponseDto validate(@NotNull @Positive Long processDefinitionId);

    void assertValid(@NotNull @Positive Long processDefinitionId);
}

