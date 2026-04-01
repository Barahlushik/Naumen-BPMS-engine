package ru.naumen.bpms.repository.criteria;

import ru.naumen.bpms.model.ProcessInstance;
import ru.naumen.bpms.model.ProcessStatus;

import java.util.List;
import java.util.Optional;

/**
 * Кастомный репозиторий для сложных выборок ProcessInstance,
 * реализованных через Criteria API.
 *
 * Используется для динамических запросов,
 * когда дефолта Spring Data недостаточно.
 */
public interface ProcessInstanceCriteriaRepository {

    /**
    * Возвращает список экземпляров процессов какого-то из вариантов статуса см. ProcessStatus,
    * принадлежащих конкретному владельцу
    */

    List<ProcessInstance> findByOwnerIdAndStatusCriteria(Long ownerId, ProcessStatus status);

     /**
     * Возвращает экземпляр процесса, который:
     * <ul>
     *     <li> находится на конкретном шаге (currentStep.id) </li>
     *     <li> принадлежит конкретному определению процесса (processDefinition.id) </li>
     * </ul>
     *
     * Фактически используется для поиска процесса в конкретном состоянии
     * внутри определённого бизнес-процесса.
     **/
    Optional<ProcessInstance> findByCurrentStepIdAndProcessDefinitionIdCriteria(Long currentStepId, Long processDefinitionId);
}