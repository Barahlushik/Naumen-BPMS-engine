package ru.naumen.bpms.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.naumen.bpms.model.ProcessDefinition;

import java.util.Optional;

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
}