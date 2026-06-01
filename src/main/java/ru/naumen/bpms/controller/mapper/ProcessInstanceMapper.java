package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.naumen.bpms.controller.dto.ProcessInstanceResponseDto;
import ru.naumen.bpms.model.ProcessInstance;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProcessInstanceMapper {

    @Mapping(target = "processDefinitionId", source = "processDefinition.id")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "currentStepId", source = "currentStep.id")
    ProcessInstanceResponseDto toDto(ProcessInstance processInstance);

    List<ProcessInstanceResponseDto> toDtoList(List<ProcessInstance> processInstances);
}

