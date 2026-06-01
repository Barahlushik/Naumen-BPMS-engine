package ru.naumen.bpms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.naumen.bpms.controller.dto.ProcessDefinitionResponseDto;
import ru.naumen.bpms.controller.dto.TransitionResponseDto;
import ru.naumen.bpms.controller.mapper.ProcessDefinitionMapper;
import ru.naumen.bpms.controller.mapper.TransitionMapper;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.service.ProcessDefinitionService;
import ru.naumen.bpms.service.ProcessInstanceService;

import java.util.List;

@RestController
@RequestMapping("/api/criteria/processes")
@Slf4j
@Tag(name = "Criteria Processes")
@SecurityRequirement(name = "sessionAuth")
public class ProcessController {

    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;
    private final ProcessDefinitionMapper processDefinitionMapper;
    private final TransitionMapper transitionMapper;

    public ProcessController(ProcessDefinitionService processDefinitionService,
                             ProcessInstanceService processInstanceService,
                             ProcessDefinitionMapper processDefinitionMapper,
                             TransitionMapper transitionMapper) {
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
        this.processDefinitionMapper = processDefinitionMapper;
        this.transitionMapper = transitionMapper;
    }

    @GetMapping("/{id}/with-steps")
    @Operation(summary = "Диагностически получить процесс с шагами")
    public ResponseEntity<ProcessDefinitionResponseDto> getProcessWithSteps(@PathVariable Long id) {
        log.info("API request: get process definition with steps. processDefinitionId={}", id);
        ProcessDefinition result = processDefinitionService.getProcessWithSteps(id);
        log.info("API response prepared: process definition with steps. processDefinitionId={}, stepsCount={}",
                id, result.getSteps().size());
        return ResponseEntity.ok(processDefinitionMapper.toDto(result));
    }

    @GetMapping("/{id}/with-steps-and-transitions")
    @Operation(summary = "Диагностически получить процесс с шагами и переходами")
    public ResponseEntity<ProcessDefinitionResponseDto> getProcessWithStepsAndTransitions(@PathVariable Long id) {
        log.info("API request: get process definition with steps and transitions. processDefinitionId={}", id);
        ProcessDefinition result = processDefinitionService.getProcessWithStepsAndTransitions(id);
        log.info("API response prepared: process definition with steps and transitions. processDefinitionId={}, stepsCount={}, transitionsCount={}",
                id, result.getSteps().size(), result.getTransitions().size());
        return ResponseEntity.ok(processDefinitionMapper.toDto(result));
    }

    @GetMapping("/by-step/{stepId}")
    @Operation(summary = "Диагностически получить процесс по id шага")
    public ResponseEntity<ProcessDefinitionResponseDto> getProcessByStepId(@PathVariable Long stepId) {
        log.info("API request: get process definition by step. stepId={}", stepId);
        ProcessDefinition result = processDefinitionService.getProcessByStepId(stepId);
        log.info("API response prepared: process definition by step. stepId={}, processDefinitionId={}",
                stepId, result.getId());
        return ResponseEntity.ok(processDefinitionMapper.toDto(result));
    }

    @GetMapping("/instances/{processInstanceId}/available-transitions")
    @Operation(summary = "Диагностически получить доступные переходы экземпляра процесса")
    public ResponseEntity<List<TransitionResponseDto>> getAvailableTransitions(@PathVariable Long processInstanceId) {
        log.info("API request: get available transitions. processInstanceId={}", processInstanceId);
        List<Transition> transitions = processInstanceService.getAvailableTransitions(processInstanceId);
        log.info("API response prepared: available transitions. processInstanceId={}, transitionsCount={}",
                processInstanceId, transitions.size());
        return ResponseEntity.ok(transitionMapper.toDtoList(transitions));
    }
}
