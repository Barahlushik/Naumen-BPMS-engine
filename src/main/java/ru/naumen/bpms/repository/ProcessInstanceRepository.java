package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;
import ru.naumen.bpms.repository.criteria.ProcessInstanceCriteriaRepository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "process-instances", collectionResourceRel = "process-instances")
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, Long>, ProcessInstanceCriteriaRepository {

    List<ProcessInstance> findByOwnerIdAndStatus(Long ownerId, ProcessStatus status);

    List<ProcessInstance> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"participants"})
    Optional<ProcessInstance> findWithParticipantsById(Long id);

    @EntityGraph(attributePaths = {"participants", "currentStep", "processDefinition"})
    Optional<ProcessInstance> findDetailedById(Long id);
}
