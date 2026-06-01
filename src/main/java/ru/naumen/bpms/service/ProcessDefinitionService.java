package ru.naumen.bpms.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;

import java.util.List;

public interface ProcessDefinitionService extends ProcessService<ProcessDefinition, Long> {

    ProcessDefinition getProcessWithSteps(@NotNull @Positive Long id);

    ProcessDefinition getProcessWithStepsAndTransitions(@NotNull @Positive Long id);

    ProcessDefinition getProcessByStepId(@NotNull @Positive Long stepId);

    List<ProcessDefinition> getAllProcessesWithStepsAndTransitions();

    ProcessDefinition updateProcess(@NotNull @Positive Long id,
                                    @NotBlank String title,
                                    String description,
                                    String category);

    List<StepDefinition> getSteps(@NotNull @Positive Long processDefinitionId);

    List<Transition> getTransitions(@NotNull @Positive Long processDefinitionId);

    StepDefinition addStep(@NotNull @Positive Long processDefinitionId,
                           @NotBlank String stepName,
                           @NotNull StepType stepType);

    StepDefinition addStepWithTransitions(@NotNull @Positive Long processDefinitionId,
                                          @NotBlank String stepName,
                                          @NotNull StepType stepType,
                                          List<@Valid Transition> transitions);

    Transition addTransition(@NotNull @Positive Long processDefinitionId,
                             @NotNull @Positive Long fromStepId,
                             @NotNull @Positive Long toStepId,
                             @NotBlank String transitionName,
                             String condition);
}
