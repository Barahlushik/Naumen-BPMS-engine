package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;

import java.util.List;
import java.util.Optional;

public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, Long> {

    List<ProcessInstance> findByOwnerIdAndStatus(Long ownerId, ProcessStatus status);

    @EntityGraph(attributePaths = {"participants"})
    Optional<ProcessInstance> findWithParticipantsById(Long id);

    @EntityGraph(attributePaths = {"participants", "currentStep", "processDefinition"})
    Optional<ProcessInstance> findDetailedById(Long id);
}