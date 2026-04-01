package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.naumen.bpms.model.StepDefinition;

import java.util.List;

public interface StepDefinitionRepository extends JpaRepository<StepDefinition, Long> {

    List<StepDefinition> findByProcessDefinitionId(Long processDefinitionId);

}
