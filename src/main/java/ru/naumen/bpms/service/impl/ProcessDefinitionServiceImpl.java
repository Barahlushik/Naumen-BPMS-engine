package ru.naumen.bpms.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.StepDefinitionRepository;
import ru.naumen.bpms.repository.TransitionRepository;
import ru.naumen.bpms.service.ProcessDefinitionService;
import ru.naumen.bpms.service.exception.process.*;

import java.util.List;

@Service
@Validated
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final StepDefinitionRepository stepDefinitionRepository;
    private final TransitionRepository transitionRepository;

    public ProcessDefinitionServiceImpl(ProcessDefinitionRepository processDefinitionRepository,
                                        StepDefinitionRepository stepDefinitionRepository,
                                        TransitionRepository transitionRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
        this.stepDefinitionRepository = stepDefinitionRepository;
        this.transitionRepository = transitionRepository;
    }

    @Override
    @Transactional
    public ProcessDefinition createProcess(ProcessDefinition process) {
        return processDefinitionRepository.save(process);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessDefinition getProcess(Long id) {
        if (id < 1) {
            throw new ProcessDefinitionValidationException(
                    "Невозможно запросить процесс с id < 1"
            );
        }
        return processDefinitionRepository.findById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessDefinition> getAllProcesses() {
        return (List<ProcessDefinition>) processDefinitionRepository.findAll();
    }

    @Override
    @Transactional
    public ProcessDefinition updateProcess(ProcessDefinition process) {
        if (process.getId() == null) {
            throw new ProcessDefinitionValidationException(
                    "Невозможно обновить определение процесса без идентификатора."
            );
        }

        ProcessDefinition existing = processDefinitionRepository.findById(process.getId())
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", process.getId())
                ));

        existing.setTitle(process.getTitle());
        existing.setDescription(process.getDescription());
        existing.setCategory(process.getCategory());

        return processDefinitionRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProcess(Long id) {
        if (id < 1) {
            throw new ProcessDefinitionValidationException(
                    "Невозможно удалить процесс с id < 1"
            );
        }
        ProcessDefinition existing = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));

        processDefinitionRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessDefinition getProcessWithSteps(Long id) {
        return processDefinitionRepository.findWithStepsById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessDefinition getProcessWithStepsAndTransitions(Long id) {
        return processDefinitionRepository.findWithStepsAndTransitionsById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StepDefinition> getSteps(Long processDefinitionId) {
        processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        return stepDefinitionRepository.findByProcessDefinitionId(processDefinitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transition> getTransitions(Long processDefinitionId) {
        processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        return transitionRepository.findByProcessDefinitionId(processDefinitionId);
    }

    @Override
    @Transactional
    public StepDefinition addStep(Long processDefinitionId,
                                  String stepName,
                                  StepType stepType) {

        ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        StepDefinition step = new StepDefinition(stepName, stepType);
        processDefinition.addStep(step);

        return stepDefinitionRepository.save(step);
    }

    @Override
    @Transactional
    public StepDefinition addStepWithTransitions(Long processDefinitionId,
                                                 String stepName,
                                                 StepType stepType,
                                                 List<Transition> transitions) {

        ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        StepDefinition step = new StepDefinition(stepName, stepType);
        step.setProcessDefinition(processDefinition);

        if (transitions != null) {
            for (Transition transition : transitions) {
                if (transition == null) {
                    throw new TransitionValidationException(
                            "Переход в списке добавляемых переходов не должен быть null."
                    );
                }

                if (transition.getToStep() == null) {
                    throw new TransitionValidationException(
                            "Целевой шаг перехода не должен быть null."
                    );
                }

                if (transition.getToStep().getProcessDefinition() == null
                        || transition.getToStep().getProcessDefinition().getId() == null) {
                    throw new StepDoesNotBelongToProcessDefinitionException(
                            String.format(
                                    "Шаг с id=%d не привязан ни к одному определению процесса.",
                                    transition.getToStep().getId()
                            )
                    );
                }

                if (!transition.getToStep().getProcessDefinition().getId().equals(processDefinition.getId())) {
                    throw new StepDoesNotBelongToProcessDefinitionException(
                            String.format(
                                    "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                                    transition.getToStep().getId(),
                                    processDefinition.getId()
                            )
                    );
                }

                transition.setProcessDefinition(processDefinition);
                step.addOutgoingTransition(transition);
            }
        } else {
            throw new TransitionValidationException(
                    "Список переходов не должен быть null."
            );
        }

        processDefinition.addStep(step);

        return stepDefinitionRepository.save(step);
    }

    @Override
    @Transactional
    public Transition addTransition(Long processDefinitionId,
                                    Long fromStepId,
                                    Long toStepId,
                                    String transitionName,
                                    String condition) {

        ProcessDefinition processDefinition = processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", processDefinitionId)
                ));

        StepDefinition fromStep = stepDefinitionRepository.findById(fromStepId)
                .orElseThrow(() -> new StepNotFoundException(
                        String.format("Шаг-источник с id=%d не найден.", fromStepId)
                ));

        StepDefinition toStep = stepDefinitionRepository.findById(toStepId)
                .orElseThrow(() -> new StepNotFoundException(
                        String.format("Шаг-назначение с id=%d не найден.", toStepId)
                ));

        if (fromStep.getProcessDefinition() == null || fromStep.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format("Шаг с id=%d не привязан ни к одному определению процесса.", fromStep.getId())
            );
        }

        if (!fromStep.getProcessDefinition().getId().equals(processDefinition.getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            fromStep.getId(),
                            processDefinition.getId()
                    )
            );
        }

        if (toStep.getProcessDefinition() == null || toStep.getProcessDefinition().getId() == null) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format("Шаг с id=%d не привязан ни к одному определению процесса.", toStep.getId())
            );
        }

        if (!toStep.getProcessDefinition().getId().equals(processDefinition.getId())) {
            throw new StepDoesNotBelongToProcessDefinitionException(
                    String.format(
                            "Шаг с id=%d не принадлежит определению процесса с id=%d.",
                            toStep.getId(),
                            processDefinition.getId()
                    )
            );
        }

        Transition transition = new Transition(toStep,fromStep, condition);
        transition.setName(transitionName);
        transition.setProcessDefinition(processDefinition);
        transition.setFromStep(fromStep);

        return transitionRepository.save(transition);
    }
}