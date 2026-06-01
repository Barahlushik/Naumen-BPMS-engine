package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.naumen.bpms.controller.dto.TransitionResponseDto;
import ru.naumen.bpms.model.Transition;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransitionMapper {

    @Mapping(target = "fromStepId", source = "fromStep.id")
    @Mapping(target = "fromStepName", source = "fromStep.name")
    @Mapping(target = "toStepId", source = "toStep.id")
    @Mapping(target = "toStepName", source = "toStep.name")
    @Mapping(target = "processDefinitionId", source = "processDefinition.id")
    TransitionResponseDto toDto(Transition transition);

    List<TransitionResponseDto> toDtoList(List<Transition> transitions);
}