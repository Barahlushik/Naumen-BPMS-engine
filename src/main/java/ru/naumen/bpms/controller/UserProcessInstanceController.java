package ru.naumen.bpms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.naumen.bpms.controller.dto.ProcessInstanceResponseDto;
import ru.naumen.bpms.controller.dto.StartProcessRequest;
import ru.naumen.bpms.controller.dto.TransitionResponseDto;
import ru.naumen.bpms.controller.mapper.ProcessInstanceMapper;
import ru.naumen.bpms.controller.mapper.TransitionMapper;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.service.ProcessInstanceService;
import ru.naumen.bpms.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/process-instances")
@Slf4j
@Tag(name = "Process Instances")
@SecurityRequirement(name = "sessionAuth")
public class UserProcessInstanceController {

    private final ProcessInstanceService processInstanceService;
    private final UserService userService;
    private final ProcessInstanceMapper processInstanceMapper;
    private final TransitionMapper transitionMapper;

    public UserProcessInstanceController(ProcessInstanceService processInstanceService,
                                         UserService userService,
                                         ProcessInstanceMapper processInstanceMapper,
                                         TransitionMapper transitionMapper) {
        this.processInstanceService = processInstanceService;
        this.userService = userService;
        this.processInstanceMapper = processInstanceMapper;
        this.transitionMapper = transitionMapper;
    }

    @PostMapping("/start")
    @Operation(summary = "Запустить экземпляр процесса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Экземпляр процесса запущен"),
            @ApiResponse(responseCode = "400", description = "Процесс невалиден или запрос некорректен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    public ResponseEntity<ProcessInstanceResponseDto> startProcess(@Valid @RequestBody StartProcessRequest request,
                                                                   Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("API request: start process. processDefinitionId={}, startStepId={}, ownerId={}",
                request.processDefinitionId(), request.startStepId(), currentUser.getId());

        ProcessInstance processInstance = processInstanceService.startProcess(
                request.processDefinitionId(),
                currentUser.getId(),
                request.startStepId()
        );

        return ResponseEntity.ok(processInstanceMapper.toDto(processInstance));
    }

    @GetMapping("/my")
    @Operation(summary = "Получить экземпляры процессов текущего пользователя")
    public ResponseEntity<List<ProcessInstanceResponseDto>> getMyProcesses(@RequestParam(required = false) ProcessStatus status,
                                                                           Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("API request: get current user process instances. ownerId={}, status={}", currentUser.getId(), status);

        List<ProcessInstance> processInstances = status == null
                ? processInstanceService.getProcessesByOwner(currentUser.getId())
                : processInstanceService.getProcessesByOwnerAndStatus(currentUser.getId(), status);

        return ResponseEntity.ok(processInstanceMapper.toDtoList(processInstances));
    }

    @GetMapping("/{id}/available-transitions")
    @Operation(summary = "Получить доступные переходы экземпляра процесса")
    public ResponseEntity<List<TransitionResponseDto>> getAvailableTransitions(@PathVariable Long id,
                                                                               Authentication authentication) {
        assertCanAccessProcess(id, authentication);

        List<Transition> transitions = processInstanceService.getAvailableTransitions(id);
        log.info("API response prepared: user available transitions. processInstanceId={}, transitionsCount={}",
                id, transitions.size());

        return ResponseEntity.ok(transitionMapper.toDtoList(transitions));
    }

    @PostMapping("/{id}/transitions/{transitionId}/execute")
    @Operation(summary = "Выполнить переход экземпляра процесса")
    public ResponseEntity<ProcessInstanceResponseDto> executeTransition(@PathVariable Long id,
                                                                       @PathVariable Long transitionId,
                                                                       Authentication authentication) {
        assertCanAccessProcess(id, authentication);

        ProcessInstance processInstance = processInstanceService.executeTransition(id, transitionId);
        return ResponseEntity.ok(processInstanceMapper.toDto(processInstance));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Завершить экземпляр процесса")
    public ResponseEntity<ProcessInstanceResponseDto> completeProcess(@PathVariable Long id,
                                                                      Authentication authentication) {
        assertCanAccessProcess(id, authentication);

        ProcessInstance processInstance = processInstanceService.completeProcess(id);
        return ResponseEntity.ok(processInstanceMapper.toDto(processInstance));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить экземпляр процесса")
    public ResponseEntity<ProcessInstanceResponseDto> cancelProcess(@PathVariable Long id,
                                                                    Authentication authentication) {
        assertCanAccessProcess(id, authentication);

        ProcessInstance processInstance = processInstanceService.cancelProcess(id);
        return ResponseEntity.ok(processInstanceMapper.toDto(processInstance));
    }

    private void assertCanAccessProcess(Long processInstanceId, Authentication authentication) {
        ProcessInstance processInstance = processInstanceService.getProcess(processInstanceId);
        User currentUser = getCurrentUser(authentication);

        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean owner = processInstance.getOwner().getId().equals(currentUser.getId());

        if (!admin && !owner) {
            log.warn("Process instance access denied. processInstanceId={}, userId={}",
                    processInstanceId, currentUser.getId());
            throw new AccessDeniedException("Нет доступа к экземпляру процесса.");
        }
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName());
    }
}
