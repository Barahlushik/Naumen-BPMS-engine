package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import ru.naumen.bpms.controller.dto.ProcessDefinitionResponseDto;
import ru.naumen.bpms.model.ProcessDefinition;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {
                StepDefinitionMapper.class,
                TransitionMapper.class
        }
)
public interface ProcessDefinitionMapper {

    ProcessDefinitionResponseDto toDto(ProcessDefinition processDefinition);

    List<ProcessDefinitionResponseDto> toDtoList(List<ProcessDefinition> processDefinitions);
}