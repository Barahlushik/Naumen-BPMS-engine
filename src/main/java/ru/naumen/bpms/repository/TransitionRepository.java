package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.naumen.bpms.model.Transition;

import java.util.List;
import java.util.Optional;

public interface TransitionRepository extends CrudRepository<Transition, Long> {


    List<Transition> findByProcessDefinitionId(Long processDefinitionId);

    List<Transition> findByFromStepId(Long fromStepId);

    Optional<Transition> findByToStepId(Long toStepId);

    @Query("""
           select t
           from Transition t
           join ProcessInstance pi on pi.currentStep = t.fromStep
           where pi.id = :processInstanceId
           """)
    List<Transition> findAvailableTransitionsByProcessInstanceId(Long processInstanceId);
}