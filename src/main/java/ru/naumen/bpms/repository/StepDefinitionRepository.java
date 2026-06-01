package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.naumen.bpms.model.StepDefinition;

import java.util.List;

@RepositoryRestResource(path = "step-definitions", collectionResourceRel = "step-definitions")
public interface StepDefinitionRepository extends JpaRepository<StepDefinition, Long> {

    List<StepDefinition> findByProcessDefinitionId(Long processDefinitionId);

}
