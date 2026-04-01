package ru.naumen.bpms.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.StepDefinitionRepository;
import ru.naumen.bpms.repository.TransitionRepository;
import ru.naumen.bpms.service.ProcessDefinitionService;

import java.util.ArrayList;
import java.util.List;

@Service
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
    public ProcessDefinition createProcess(ProcessDefinition process) {
        validateProcessDefinition(process);
        return processDefinitionRepository.save(process);
    }

    @Override
    public ProcessDefinition getProcess(Long id) {
        validateId(id, "ProcessDefinition id");
        return processDefinitionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessDefinition with id=" + id + " not found."));
    }

    @Override
    public List<ProcessDefinition> getAllProcesses() {
        return toList(processDefinitionRepository.findAll());
    }

    @Override
    public ProcessDefinition updateProcess(ProcessDefinition process) {
        validateProcessDefinition(process);

        ProcessDefinition existing = getProcess(process.getId());
        existing.setTitle(process.getTitle());
        existing.setDescription(process.getDescription());

        return processDefinitionRepository.save(existing);
    }

    @Override
    public void deleteProcess(Long id) {
        validateId(id, "ProcessDefinition id");

        if (!processDefinitionRepository.existsById(id)) {
            throw new EntityNotFoundException("ProcessDefinition with id=" + id + " not found.");
        }

        processDefinitionRepository.deleteById(id);
    }

    @Override
    public ProcessDefinition getProcessWithSteps(Long id) {
        validateId(id, "ProcessDefinition id");
        return processDefinitionRepository.findWithStepsById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessDefinition with id=" + id + " not found."));
    }

    @Override
    public ProcessDefinition getProcessWithStepsAndTransitions(Long id) {
        validateId(id, "ProcessDefinition id");
        return processDefinitionRepository.findWithStepsAndTransitionsById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProcessDefinition with id=" + id + " not found."));
    }

    @Override
    public List<StepDefinition> getSteps(Long processDefinitionId) {
        validateId(processDefinitionId, "ProcessDefinition id");
        return stepDefinitionRepository.findByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public List<Transition> getTransitions(Long processDefinitionId) {
        validateId(processDefinitionId, "ProcessDefinition id");
        return transitionRepository.findByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public StepDefinition addStep(Long processDefinitionId,
                                  String stepName,
                                  StepType stepType) {
        validateId(processDefinitionId, "ProcessDefinition id");

        if (stepName == null || stepName.isBlank()) {
            throw new ValidationException("Step name must not be blank.");
        }

        if (stepType == null) {
            throw new ValidationException("Step type must not be null.");
        }

        ProcessDefinition processDefinition = getProcess(processDefinitionId);

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
        validateId(processDefinitionId, "ProcessDefinition id");

        if (stepName == null || stepName.isBlank()) {
            throw new ValidationException("Step name must not be blank.");
        }

        if (stepType == null) {
            throw new ValidationException("Step type must not be null.");
        }

        ProcessDefinition processDefinition = getProcess(processDefinitionId);

        StepDefinition step = new StepDefinition(stepName, stepType);
        step.setProcessDefinition(processDefinition);

        if (transitions != null) {
            for (Transition transition : transitions) {
                if (transition == null) {
                    throw new ValidationException("Transition must not be null.");
                }
                if (transition.getToStep() == null) {
                    throw new ValidationException("Transition target step must not be null.");
                }

                transition.setProcessDefinition(processDefinition);
                step.addOutgoingTransition(transition);
            }
        }

        processDefinition.addStep(step);

        return stepDefinitionRepository.save(step);
    }

    @Override
    public Transition addTransition(Long processDefinitionId,
                                    Long fromStepId,
                                    Long toStepId,
                                    String transitionName,
                                    String condition) {
        validateId(processDefinitionId, "ProcessDefinition id");
        validateId(fromStepId, "FromStep id");
        validateId(toStepId, "ToStep id");

        if (transitionName == null || transitionName.isBlank()) {
            throw new ValidationException("Transition name must not be blank.");
        }

        ProcessDefinition processDefinition = getProcess(processDefinitionId);

        StepDefinition fromStep = stepDefinitionRepository.findById(fromStepId)
                .orElseThrow(() -> new EntityNotFoundException("StepDefinition with id=" + fromStepId + " not found."));

        StepDefinition toStep = stepDefinitionRepository.findById(toStepId)
                .orElseThrow(() -> new EntityNotFoundException("StepDefinition with id=" + toStepId + " not found."));

        Transition transition = new Transition(toStep, condition);
        transition.setName(transitionName);
        transition.setProcessDefinition(processDefinition);
        transition.setFromStep(fromStep);

        return transitionRepository.save(transition);
    }

    private void validateProcessDefinition(ProcessDefinition process) {
        if (process == null) {
            throw new ValidationException("ProcessDefinition must not be null.");
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ValidationException(fieldName + " must be positive.");
        }
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }
}