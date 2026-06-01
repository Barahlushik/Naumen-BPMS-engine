package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import ru.naumen.bpms.controller.dto.StepDefinitionResponseDto;
import ru.naumen.bpms.model.StepDefinition;

@Mapper(componentModel = "spring")
public interface StepDefinitionMapper {

    StepDefinitionResponseDto toDto(StepDefinition stepDefinition);
}
