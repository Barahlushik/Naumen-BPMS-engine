package ru.naumen.bpms.service.impl;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        ProcessDefinition savedProcess = processDefinitionRepository.save(process);
        log.info("Process definition created. processDefinitionId={}, title={}",
                savedProcess.getId(), savedProcess.getTitle());
        return savedProcess;
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

        ProcessDefinition savedProcess = processDefinitionRepository.save(existing);
        log.info("Process definition updated. processDefinitionId={}, title={}",
                savedProcess.getId(), savedProcess.getTitle());
        return savedProcess;
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
        log.info("Process definition deleted. processDefinitionId={}", id);
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
    public ProcessDefinition getProcessByStepId(Long stepId) {
        ProcessDefinition processDefinition = processDefinitionRepository.findByStepId(stepId)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса по stepId=%d не найдено.", stepId)
                ));

        log.info("Process definition found by step. stepId={}, processDefinitionId={}",
                stepId, processDefinition.getId());
        return processDefinition;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessDefinition> getAllProcessesWithStepsAndTransitions() {
        List<ProcessDefinition> processDefinitions = processDefinitionRepository.findAllWithStepsAndTransitions();
        log.info("Process definitions loaded with steps and transitions. processDefinitionsCount={}",
                processDefinitions.size());
        return processDefinitions;
    }

    @Override
    @Transactional
    public ProcessDefinition updateProcess(Long id,
                                           String title,
                                           String description,
                                           String category) {
        ProcessDefinition existing = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new ProcessDefinitionNotFoundException(
                        String.format("Определение процесса с id=%d не найдено.", id)
                ));

        existing.setTitle(title);
        existing.setDescription(description);
        existing.setCategory(category);

        ProcessDefinition savedProcess = processDefinitionRepository.save(existing);
        log.info("Process definition updated. processDefinitionId={}, title={}",
                savedProcess.getId(), savedProcess.getTitle());
        return savedProcess;
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

        StepDefinition savedStep = stepDefinitionRepository.save(step);
        log.info("Step added to process definition. processDefinitionId={}, stepId={}, stepType={}",
                processDefinitionId, savedStep.getId(), savedStep.getType());
        return savedStep;
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

        StepDefinition savedStep = stepDefinitionRepository.save(step);
        log.info("Step with transitions added to process definition. processDefinitionId={}, stepId={}, transitionsCount={}",
                processDefinitionId, savedStep.getId(), transitions.size());
        return savedStep;
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

        Transition transition = new Transition(fromStep, toStep, condition);
        transition.setName(transitionName);
        transition.setProcessDefinition(processDefinition);
        transition.setFromStep(fromStep);

        Transition savedTransition = transitionRepository.save(transition);
        log.info("Transition added to process definition. processDefinitionId={}, transitionId={}, fromStepId={}, toStepId={}",
                processDefinitionId, savedTransition.getId(), fromStepId, toStepId);
        return savedTransition;
    }
}
