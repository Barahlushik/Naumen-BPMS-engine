package ru.naumen.bpms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.naumen.bpms.controller.dto.ProcessValidationResponseDto;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.service.ProcessDefinitionValidationService;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionNotFoundException;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionValidationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@Slf4j
public class ProcessDefinitionValidationServiceImpl implements ProcessDefinitionValidationService {

    private final ProcessDefinitionRepository processDefinitionRepository;

    public ProcessDefinitionValidationServiceImpl(ProcessDefinitionRepository processDefinitionRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessValidationResponseDto validate(Long processDefinitionId) {
        ProcessDefinition processDefinition = processDefinitionRepository.findWithStepsAndTransitionsById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Set<StepDefinition> steps = processDefinition.getSteps() != null
                ? processDefinition.getSteps()
                : Collections.emptySet();
        Set<Transition> transitions = processDefinition.getTransitions() != null
                ? processDefinition.getTransitions()
                : Collections.emptySet();

        if (steps.isEmpty()) {
            errors.add("Процесс не содержит шагов.");
        }

        List<StepDefinition> startSteps = findStepsByType(steps, StepType.START_EVENT);
        List<StepDefinition> endSteps = findStepsByType(steps, StepType.END_EVENT);

        if (startSteps.isEmpty()) {
            errors.add("Процесс не содержит стартовый шаг START_EVENT.");
        }
        if (startSteps.size() > 1) {
            warnings.add("Процесс содержит больше одного стартового шага START_EVENT.");
        }
        if (endSteps.isEmpty()) {
            errors.add("Процесс не содержит конечный шаг END_EVENT.");
        }

        if (transitions.isEmpty() && !steps.isEmpty()) {
            errors.add("Процесс не содержит переходов.");
        }

        Set<Long> stepIds = steps.stream()
                .map(StepDefinition::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, List<Long>> graph = new HashMap<>();
        for (StepDefinition step : steps) {
            if (step.getId() != null) {
                graph.put(step.getId(), new ArrayList<>());
            }
        }

        validateTransitions(processDefinitionId, transitions, stepIds, graph, errors);

        if (!startSteps.isEmpty() && !endSteps.isEmpty()) {
            Set<Long> reachableStepIds = findReachableStepIds(startSteps, graph);
            boolean endReachable = endSteps.stream()
                    .map(StepDefinition::getId)
                    .filter(Objects::nonNull)
                    .anyMatch(reachableStepIds::contains);

            if (!endReachable) {
                errors.add("Ни один конечный шаг END_EVENT не достижим из стартового шага.");
            }

            List<Long> unreachableStepIds = stepIds.stream()
                    .filter(stepId -> !reachableStepIds.contains(stepId))
                    .sorted()
                    .toList();
            if (!unreachableStepIds.isEmpty()) {
                warnings.add("В процессе есть недостижимые шаги: " + unreachableStepIds + ".");
            }
        }

        ProcessValidationResponseDto result = new ProcessValidationResponseDto(
                processDefinitionId,
                errors.isEmpty(),
                List.copyOf(errors),
                List.copyOf(warnings)
        );

        log.info("Process definition validation completed. processDefinitionId={}, valid={}, errorsCount={}, warningsCount={}",
                result.processDefinitionId(), result.valid(), result.errors().size(), result.warnings().size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public void assertValid(Long processDefinitionId) {
        ProcessValidationResponseDto result = validate(processDefinitionId);
        if (!result.valid()) {
            throw new ProcessDefinitionValidationException(
                    "Определение процесса невалидно: " + String.join("; ", result.errors())
            );
        }
    }

    private List<StepDefinition> findStepsByType(Set<StepDefinition> steps, StepType type) {
        return steps.stream()
                .filter(step -> step.getType() == type)
                .toList();
    }

    private void validateTransitions(Long processDefinitionId,
                                     Set<Transition> transitions,
                                     Set<Long> stepIds,
                                     Map<Long, List<Long>> graph,
                                     List<String> errors) {
        for (Transition transition : transitions) {
            Long transitionId = transition.getId();

            if (transition.getProcessDefinition() == null
                    || transition.getProcessDefinition().getId() == null
                    || !processDefinitionId.equals(transition.getProcessDefinition().getId())) {
                errors.add(String.format("Переход id=%s не принадлежит процессу id=%d.", transitionId, processDefinitionId));
            }

            if (transition.getFromStep() == null) {
                errors.add(String.format("У перехода id=%s отсутствует исходный шаг.", transitionId));
                continue;
            }

            if (transition.getToStep() == null) {
                errors.add(String.format("У перехода id=%s отсутствует целевой шаг.", transitionId));
                continue;
            }

            Long fromStepId = transition.getFromStep().getId();
            Long toStepId = transition.getToStep().getId();

            if (!stepIds.contains(fromStepId)) {
                errors.add(String.format("Исходный шаг id=%s перехода id=%s не входит в процесс id=%d.",
                        fromStepId, transitionId, processDefinitionId));
            }
            if (!stepIds.contains(toStepId)) {
                errors.add(String.format("Целевой шаг id=%s перехода id=%s не входит в процесс id=%d.",
                        toStepId, transitionId, processDefinitionId));
            }

            if (stepIds.contains(fromStepId) && stepIds.contains(toStepId)) {
                graph.computeIfAbsent(fromStepId, ignored -> new ArrayList<>()).add(toStepId);
            }
        }
    }

    private Set<Long> findReachableStepIds(List<StepDefinition> startSteps,
                                           Map<Long, List<Long>> graph) {
        Set<Long> visited = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();

        for (StepDefinition startStep : startSteps) {
            if (startStep.getId() != null) {
                queue.add(startStep.getId());
            }
        }

        while (!queue.isEmpty()) {
            Long stepId = queue.removeFirst();
            if (!visited.add(stepId)) {
                continue;
            }

            for (Long nextStepId : graph.getOrDefault(stepId, Collections.emptyList())) {
                if (!visited.contains(nextStepId)) {
                    queue.addLast(nextStepId);
                }
            }
        }

        return visited;
    }
}

