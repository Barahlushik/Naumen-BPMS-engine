package ru.naumen.bpms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.TransitionRepository;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/criteria/processes")
public class ProcessController {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final TransitionRepository transitionRepository;

    public ProcessController(ProcessDefinitionRepository processDefinitionRepository,
                                  TransitionRepository transitionRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
        this.transitionRepository = transitionRepository;
    }

    @GetMapping("/{id}/with-steps")
    public ResponseEntity<ProcessDefinition> getProcessWithSteps(@PathVariable Long id) {
        ProcessDefinition result = processDefinitionRepository.findWithStepsById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/with-steps-and-transitions")
    public ResponseEntity<ProcessDefinition> getProcessWithStepsAndTransitions(@PathVariable Long id) {
        ProcessDefinition result = processDefinitionRepository.findWithStepsAndTransitionsById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-step/{stepId}")
    public ResponseEntity<ProcessDefinition> getProcessByStepId(@PathVariable Long stepId) {
        ProcessDefinition result = processDefinitionRepository.findByStepId(stepId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса по stepId=%d не найдено.", stepId)
                ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/instances/{processInstanceId}/available-transitions")
    public ResponseEntity<List<Transition>> getAvailableTransitions(@PathVariable Long processInstanceId) {
        List<Transition> transitions = transitionRepository.findAvailableTransitionsByProcessInstanceId(processInstanceId);
        return ResponseEntity.ok(transitions);
    }
}