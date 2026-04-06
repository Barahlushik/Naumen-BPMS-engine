package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.naumen.bpms.model.ProcessDefinition;

import java.util.List;
import java.util.Optional;


@RepositoryRestResource(path = "process-definitions", collectionResourceRel = "process-definitions")
public interface ProcessDefinitionRepository extends CrudRepository<ProcessDefinition, Long> {

    @Query("""
        SELECT DISTINCT pd
        FROM ProcessDefinition pd
        LEFT JOIN FETCH pd.steps s
        WHERE pd.id = :id
    """)
    Optional<ProcessDefinition> findWithStepsById(Long id);

    @Query("""
        SELECT DISTINCT pd
        FROM ProcessDefinition pd
        LEFT JOIN FETCH pd.steps s
        LEFT JOIN FETCH pd.transitions t
        WHERE pd.id = :id
    """)
    Optional<ProcessDefinition> findWithStepsAndTransitionsById(Long id);

    @Query("""
        SELECT DISTINCT pd
        FROM ProcessDefinition pd
        JOIN pd.steps s
        WHERE s.id = :stepId
    """)
    Optional<ProcessDefinition> findByStepId(Long stepId);

    @Query("""
        select distinct pd
        from ProcessDefinition pd
        left join fetch pd.steps s
        left join fetch s.outgoingTransitions ot
        left join fetch ot.toStep ts
        order by pd.id
    """)
    List<ProcessDefinition> findAllWithStepsAndTransitions();
}