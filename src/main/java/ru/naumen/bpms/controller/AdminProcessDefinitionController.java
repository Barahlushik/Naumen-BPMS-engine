package ru.naumen.bpms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.naumen.bpms.controller.dto.*;
import ru.naumen.bpms.controller.mapper.ProcessDefinitionMapper;
import ru.naumen.bpms.controller.mapper.ProcessInstanceMapper;
import ru.naumen.bpms.controller.mapper.StepDefinitionMapper;
import ru.naumen.bpms.controller.mapper.TransitionMapper;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.service.ProcessDefinitionService;
import ru.naumen.bpms.service.ProcessDefinitionValidationService;
import ru.naumen.bpms.service.ProcessInstanceService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@Tag(name = "Admin Process Definitions")
@SecurityRequirement(name = "sessionAuth")
public class AdminProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;
    private final ProcessDefinitionValidationService processDefinitionValidationService;
    private final ProcessInstanceService processInstanceService;
    private final ProcessDefinitionMapper processDefinitionMapper;
    private final StepDefinitionMapper stepDefinitionMapper;
    private final TransitionMapper transitionMapper;
    private final ProcessInstanceMapper processInstanceMapper;

    public AdminProcessDefinitionController(ProcessDefinitionService processDefinitionService,
                                            ProcessDefinitionValidationService processDefinitionValidationService,
                                            ProcessInstanceService processInstanceService,
                                            ProcessDefinitionMapper processDefinitionMapper,
                                            StepDefinitionMapper stepDefinitionMapper,
                                            TransitionMapper transitionMapper,
                                            ProcessInstanceMapper processInstanceMapper) {
        this.processDefinitionService = processDefinitionService;
        this.processDefinitionValidationService = processDefinitionValidationService;
        this.processInstanceService = processInstanceService;
        this.processDefinitionMapper = processDefinitionMapper;
        this.stepDefinitionMapper = stepDefinitionMapper;
        this.transitionMapper = transitionMapper;
        this.processInstanceMapper = processInstanceMapper;
    }

    @GetMapping("/process-definitions")
    @Operation(summary = "Получить все определения процессов")
    public ResponseEntity<List<ProcessDefinitionResponseDto>> getProcessDefinitions() {
        List<ProcessDefinition> processDefinitions = processDefinitionService.getAllProcessesWithStepsAndTransitions();
        return ResponseEntity.ok(processDefinitionMapper.toDtoList(processDefinitions));
    }

    @GetMapping("/process-definitions/{id}")
    @Operation(summary = "Получить определение процесса по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Определение процесса найдено"),
            @ApiResponse(responseCode = "404", description = "Определение процесса не найдено")
    })
    public ResponseEntity<ProcessDefinitionResponseDto> getProcessDefinition(@PathVariable Long id) {
        ProcessDefinition processDefinition = processDefinitionService.getProcessWithStepsAndTransitions(id);
        return ResponseEntity.ok(processDefinitionMapper.toDto(processDefinition));
    }

    @PostMapping("/process-definitions")
    @Operation(summary = "Создать определение процесса")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Определение процесса создано"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    })
    public ResponseEntity<ProcessDefinitionResponseDto> createProcessDefinition(
            @Valid @RequestBody CreateProcessDefinitionRequest request
    ) {
        ProcessDefinition processDefinition = new ProcessDefinition(request.title(), request.description());
        processDefinition.setCategory(request.category());

        ProcessDefinition created = processDefinitionService.createProcess(processDefinition);
        log.info("API response created: process definition. processDefinitionId={}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(processDefinitionMapper.toDto(created));
    }

    @PutMapping("/process-definitions/{id}")
    @Operation(summary = "Обновить определение процесса")
    public ResponseEntity<ProcessDefinitionResponseDto> updateProcessDefinition(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProcessDefinitionRequest request
    ) {
        ProcessDefinition updated = processDefinitionService.updateProcess(
                id,
                request.title(),
                request.description(),
                request.category()
        );

        return ResponseEntity.ok(processDefinitionMapper.toDto(updated));
    }

    @DeleteMapping("/process-definitions/{id}")
    @Operation(summary = "Удалить определение процесса")
    public ResponseEntity<Void> deleteProcessDefinition(@PathVariable Long id) {
        processDefinitionService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/process-definitions/{id}/steps")
    @Operation(summary = "Добавить шаг в определение процесса")
    public ResponseEntity<StepDefinitionResponseDto> addStep(@PathVariable Long id,
                                                             @Valid @RequestBody CreateStepRequest request) {
        StepDefinition step = processDefinitionService.addStep(id, request.name(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(stepDefinitionMapper.toDto(step));
    }

    @PostMapping("/process-definitions/{id}/transitions")
    @Operation(summary = "Добавить переход между шагами")
    public ResponseEntity<TransitionResponseDto> addTransition(@PathVariable Long id,
                                                               @Valid @RequestBody CreateTransitionRequest request) {
        Transition transition = processDefinitionService.addTransition(
                id,
                request.fromStepId(),
                request.toStepId(),
                request.name(),
                request.condition()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transitionMapper.toDto(transition));
    }

    @PostMapping("/process-definitions/{id}/validate")
    @Operation(summary = "Проверить структуру процесса")
    public ResponseEntity<ProcessValidationResponseDto> validateProcessDefinition(@PathVariable Long id) {
        ProcessValidationResponseDto validationResult = processDefinitionValidationService.validate(id);
        return ResponseEntity.ok(validationResult);
    }

    @GetMapping("/process-instances")
    @Operation(summary = "Получить все экземпляры процессов")
    public ResponseEntity<List<ProcessInstanceResponseDto>> getProcessInstances() {
        List<ProcessInstance> processInstances = processInstanceService.getAllProcesses();
        return ResponseEntity.ok(processInstanceMapper.toDtoList(processInstances));
    }
}
