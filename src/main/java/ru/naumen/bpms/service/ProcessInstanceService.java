package ru.naumen.bpms.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;
import ru.naumen.bpms.model.Transition;

import java.util.List;

public interface ProcessInstanceService extends ProcessService<ProcessInstance, Long> {

    ProcessInstance startProcess(@NotNull @Positive  Long processDefinitionId,
                                 @NotNull @Positive  Long ownerId,
                                 @NotNull @Positive  Long startStepId);

    List<ProcessInstance> getProcessesByOwnerAndStatus(@NotNull @Positive Long ownerId,
                                                       @NotNull ProcessStatus status);

    List<ProcessInstance> getProcessesByOwner(@NotNull @Positive Long ownerId);

    List<Transition> getAvailableTransitions(@NotNull @Positive Long processInstanceId);

    ProcessInstance moveToStep(@NotNull @Positive Long processInstanceId,
                               @NotNull @Positive Long nextStepId);

    ProcessInstance executeTransition(@NotNull @Positive Long processInstanceId,
                                      @NotNull @Positive Long transitionId);

    ProcessInstance completeProcess(@NotNull @Positive Long processInstanceId);

    ProcessInstance cancelProcess(@NotNull @Positive Long processInstanceId);

    ProcessInstance addParticipant(@NotNull @Positive Long processInstanceId,
                                   @NotNull @Positive Long userId);

    ProcessInstance removeParticipant(@NotNull @Positive Long processInstanceId,
                                      @NotNull @Positive Long userId);
}
