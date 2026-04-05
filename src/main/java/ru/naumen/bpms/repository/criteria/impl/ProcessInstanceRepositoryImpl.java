package ru.naumen.bpms.repository.criteria.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;
import ru.naumen.bpms.repository.criteria.ProcessInstanceCriteriaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProcessInstanceRepositoryImpl implements ProcessInstanceCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProcessInstance> findByOwnerIdAndStatusCriteria(Long ownerId, ProcessStatus status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProcessInstance> cq = cb.createQuery(ProcessInstance.class);

        Root<ProcessInstance> processInstance = cq.from(ProcessInstance.class);

        Predicate ownerPredicate = cb.equal(processInstance.get("owner").get("id"), ownerId);
        Predicate statusPredicate = cb.equal(processInstance.get("status"), status);

        cq.select(processInstance)
                .where(cb.and(ownerPredicate, statusPredicate));

        TypedQuery<ProcessInstance> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public Optional<ProcessInstance> findByCurrentStepIdAndProcessDefinitionIdCriteria(Long currentStepId,
                                                                                       Long processDefinitionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProcessInstance> cq = cb.createQuery(ProcessInstance.class);

        Root<ProcessInstance> processInstance = cq.from(ProcessInstance.class);

        Predicate currentStepPredicate =
                cb.equal(processInstance.get("currentStep").get("id"), currentStepId);

        Predicate processDefinitionPredicate =
                cb.equal(processInstance.get("processDefinition").get("id"), processDefinitionId);

        cq.select(processInstance)
                .where(cb.and(currentStepPredicate, processDefinitionPredicate));

        TypedQuery<ProcessInstance> query = entityManager.createQuery(cq);
        List<ProcessInstance> result = query.getResultList();

        return result.stream().findFirst();
    }
}