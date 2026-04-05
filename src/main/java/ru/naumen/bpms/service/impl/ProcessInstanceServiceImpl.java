package ru.naumen.bpms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.naumen.bpms.model.*;
import ru.naumen.bpms.repository.*;
import ru.naumen.bpms.service.ProcessInstanceService;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionNotFoundException;
import ru.naumen.bpms.service.exception.process.ProcessInstanceException;
import ru.naumen.bpms.service.exception.process.StepDoesNotBelongToProcessDefinitionException;
import ru.naumen.bpms.service.exception.process.TransitionNotFoundException;
import ru.naumen.bpms.service.exception.user.UserNotFoundException;

import java.util.List;

@Service
@Validated
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final StepDefinitionRepository stepDefinitionRepository;
    private final TransitionRepository transitionRepository;
    private final UserRepository userRepository;

    public ProcessInstanceServiceImpl(ProcessInstanceRepository processInstanceRepository,
                                      ProcessDefinitionRepository processDefinitionRepository,
                                      StepDefinitionRepository stepDefinitionRepository,
                                      TransitionRepository transitionRepository,
                                      UserRepository userRepository) {
        this.processInstanceRepository = processInstanceRepository;
        this.processDefinitionRepository = processDefinitionRepository;
        this.stepDefinitionRepository = stepDefinitionRepository;
        this.transitionRepository = transitionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ProcessInstance createProcess(ProcessInstance process) {
        if (process == null) {
            throw new ProcessInstanceException("Экземпляр процесса не должен быть null.");
        }
        if (process.getProcessDefinition() == null) {
            throw new ProcessInstanceException("У экземпляра процесса отсутствует определение процесса.");
        }
        if (process.getOwner() == null) {
            throw new ProcessInstanceException("У экземпляра процесса отсутствует владелец.");
        }
        if (process.getCurrentStep() == null) {
            throw new ProcessInstanceException("У экземпляра процесса отсутствует текущий шаг.");
        }

        if (process.getCurrentStep().getProcessDefinition() == null || process.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность шага определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!process.getProcessDefinition().getId().equals(process.getCurrentStep().getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            process.getCurrentStep().getId(),
                            process.getProcessDefinition().getId()
                    )
            );
        }

        return processInstanceRepository.save(process);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessInstance getProcess(Long id) {
        return processInstanceRepository.findById(id)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", id)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessInstance> getAllProcesses() {
        return processInstanceRepository.findAll();
    }

    @Override
    @Transactional
    public ProcessInstance updateProcess(ProcessInstance process) {
        if (process == null) {
            throw new ProcessInstanceException("Экземпляр процесса не должен быть null.");
        }
        if (process.getId() == null) {
            throw new ProcessInstanceException("Идентификатор экземпляра процесса не должен быть null.");
        }

        ProcessInstance existing = processInstanceRepository.findById(process.getId())
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", process.getId())
                ));

        if (process.getProcessDefinition() == null) {
            throw new ProcessInstanceException("Определение процесса не должно быть null.");
        }
        if (process.getOwner() == null) {
            throw new ProcessInstanceException("Владелец процесса не должен быть null.");
        }
        if (process.getCurrentStep() == null) {
            throw new ProcessInstanceException("Текущий шаг процесса не должен быть null.");
        }

        if (process.getCurrentStep().getProcessDefinition() == null || process.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность шага определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!process.getProcessDefinition().getId().equals(process.getCurrentStep().getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            process.getCurrentStep().getId(),
                            process.getProcessDefinition().getId()
                    )
            );
        }

        existing.setProcessDefinition(process.getProcessDefinition());
        existing.setOwner(process.getOwner());
        existing.setCurrentStep(process.getCurrentStep());

        return processInstanceRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProcess(Long id) {
        ProcessInstance existing = processInstanceRepository.findById(id)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", id)
                ));

        processInstanceRepository.delete(existing);
    }

    /**
     * Транзакционная операция старта процесса.
     *
     * В рамках одной транзакции:
     * 1. проверяется наличие ProcessDefinition, owner и стартового шага;
     * 2. проверяется, что стартовый шаг принадлежит выбранному определению процесса;
     * 3. создаётся и сохраняется ProcessInstance;
     * 4. после сохранения дополняется состав участников;
     * 5. если на любой стадии возникает ошибка, транзакция откатывается полностью.
     */
    @Override
    @Transactional
    public ProcessInstance startProcess(Long processDefinitionId,
                                        Long ownerId,
                                        Long startStepId) {

        ProcessDefinition definition = processDefinitionRepository.findWithStepsById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь-владелец с id=%d не найден.", ownerId)
                ));

        StepDefinition startStep = stepDefinitionRepository.findById(startStepId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Шаг с id=%d не найден.", startStepId)
                ));

        if (startStep.getProcessDefinition() == null || definition.getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность шага определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!definition.getId().equals(startStep.getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            startStep.getId(),
                            definition.getId()
                    )
            );
        }

        ProcessInstance processInstance = new ProcessInstance(definition, owner, startStep);

        ProcessInstance savedInstance = processInstanceRepository.saveAndFlush(processInstance);

        if (!owner.isActive()) {
            throw new ProcessInstanceException(
                    String.format("Пользователь с id=%d неактивен и не может участвовать в процессе.", owner.getId())
            );
        }

        savedInstance.addParticipant(owner);

        return processInstanceRepository.save(savedInstance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessInstance> getProcessesByOwnerAndStatus(Long ownerId,
                                                              ProcessStatus status) {
        return processInstanceRepository.findByOwnerIdAndStatus(ownerId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transition> getAvailableTransitions(Long processInstanceId) {
        processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        return transitionRepository.findAvailableTransitionsByProcessInstanceId(processInstanceId);
    }

    @Override
    @Transactional
    public ProcessInstance moveToStep(Long processInstanceId,
                                      Long nextStepId) {

        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        if (processInstance.getStatus() == ProcessStatus.COMPLETED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d уже завершён и не может быть изменён.", processInstance.getId())
            );
        }

        if (processInstance.getStatus() == ProcessStatus.CANCELLED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d отменён и не может быть изменён.", processInstance.getId())
            );
        }

        StepDefinition nextStep = stepDefinitionRepository.findById(nextStepId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Шаг с id=%d не найден.", nextStepId)
                ));

        if (nextStep.getProcessDefinition() == null || processInstance.getProcessDefinition() == null
                || processInstance.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность шага определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!processInstance.getProcessDefinition().getId().equals(nextStep.getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            nextStep.getId(),
                            processInstance.getProcessDefinition().getId()
                    )
            );
        }

        processInstance.moveToStep(nextStep);
        return processInstanceRepository.save(processInstance);
    }

    @Override
    @Transactional
    public ProcessInstance executeTransition(Long processInstanceId,
                                             Long transitionId) {

        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        if (processInstance.getStatus() == ProcessStatus.COMPLETED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d уже завершён и не может быть изменён.", processInstance.getId())
            );
        }

        if (processInstance.getStatus() == ProcessStatus.CANCELLED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d отменён и не может быть изменён.", processInstance.getId())
            );
        }

        Transition transition = transitionRepository.findById(transitionId)
                .orElseThrow(() -> new TransitionNotFoundException(
                        String.format("Переход с id=%d не найден.", transitionId)
                ));

        if (processInstance.getCurrentStep() == null) {
            throw new ProcessInstanceException(
                    String.format("У экземпляра процесса с id=%d отсутствует текущий шаг.", processInstanceId)
            );
        }

        if (transition.getFromStep() == null || transition.getFromStep().getProcessDefinition() == null
                || processInstance.getProcessDefinition() == null
                || processInstance.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность исходного шага перехода определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!processInstance.getProcessDefinition().getId().equals(transition.getFromStep().getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            transition.getFromStep().getId(),
                            processInstance.getProcessDefinition().getId()
                    )
            );
        }

        if (transition.getToStep() == null || transition.getToStep().getProcessDefinition() == null
                || processInstance.getProcessDefinition() == null
                || processInstance.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    "Невозможно проверить принадлежность целевого шага перехода определению процесса: отсутствуют обязательные связи."
            );
        }

        if (!processInstance.getProcessDefinition().getId().equals(transition.getToStep().getProcessDefinition().getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            transition.getToStep().getId(),
                            processInstance.getProcessDefinition().getId()
                    )
            );
        }

        if (!transition.getFromStep().getId().equals(processInstance.getCurrentStep().getId())) {
            throw new ProcessInstanceException(
                    String.format(
                            "Переход с id=%d недоступен из текущего шага id=%d для экземпляра процесса id=%d.",
                            transitionId,
                            processInstance.getCurrentStep().getId(),
                            processInstanceId
                    )
            );
        }

        processInstance.moveToStep(transition.getToStep());
        return processInstanceRepository.save(processInstance);
    }

    @Override
    @Transactional
    public ProcessInstance completeProcess(Long processInstanceId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        if (processInstance.getStatus() == ProcessStatus.COMPLETED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d уже завершён.", processInstanceId)
            );
        }

        if (processInstance.getStatus() == ProcessStatus.CANCELLED) {
            throw new ProcessInstanceException(
                    String.format("Нельзя завершить отменённый экземпляр процесса с id=%d.", processInstanceId)
            );
        }

        processInstance.complete();
        return processInstanceRepository.save(processInstance);
    }

    @Override
    @Transactional
    public ProcessInstance cancelProcess(Long processInstanceId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        if (processInstance.getStatus() == ProcessStatus.CANCELLED) {
            throw new ProcessInstanceException(
                    String.format("Экземпляр процесса с id=%d уже отменён.", processInstanceId)
            );
        }

        if (processInstance.getStatus() == ProcessStatus.COMPLETED) {
            throw new ProcessInstanceException(
                    String.format("Нельзя отменить завершённый экземпляр процесса с id=%d.", processInstanceId)
            );
        }

        processInstance.cancel();
        return processInstanceRepository.save(processInstance);
    }

    @Override
    @Transactional
    public ProcessInstance addParticipant(Long processInstanceId,
                                          Long userId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь с id=%d не найден.", userId)
                ));

        if (processInstance.getParticipants() != null && processInstance.getParticipants().contains(user)) {
            throw new ProcessInstanceException(
                    String.format("Пользователь с id=%d уже является участником процесса id=%d.", userId, processInstanceId)
            );
        }

        processInstance.addParticipant(user);
        return processInstanceRepository.save(processInstance);
    }

    @Override
    @Transactional
    public ProcessInstance removeParticipant(Long processInstanceId,
                                             Long userId) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new ProcessInstanceException(
                        String.format("Экземпляр процесса с id=%d не найден.", processInstanceId)
                ));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь с id=%d не найден.", userId)
                ));

        if (processInstance.getParticipants() == null || !processInstance.getParticipants().contains(user)) {
            throw new ProcessInstanceException(
                    String.format("Пользователь с id=%d не является участником процесса id=%d.", userId, processInstanceId)
            );
        }

        processInstance.removeParticipant(user);
        return processInstanceRepository.save(processInstance);
    }
}