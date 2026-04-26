package ru.naumen.bpms.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.StepDefinitionRepository;
import ru.naumen.bpms.repository.TransitionRepository;
import ru.naumen.bpms.service.exception.process.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProcessDefinitionServiceImplIntegrationTest {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private TransitionRepository transitionRepository;

    @Test
    @DisplayName("createProcess должен сохранять определение процесса")
    void createProcess_shouldSaveProcessDefinition() {
        ProcessDefinition definition = new ProcessDefinition("Expense Approval", "Expense approval process");
        definition.setCategory("Finance");

        ProcessDefinition saved = processDefinitionService.createProcess(definition);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Expense Approval");
        assertThat(saved.getDescription()).isEqualTo("Expense approval process");
        assertThat(saved.getCategory()).isEqualTo("Finance");
    }

    @Test
    @DisplayName("getProcess должен возвращать определение процесса по id")
    void getProcess_shouldReturnProcessDefinitionById() {
        ProcessDefinition saved = processDefinitionRepository.save(
                new ProcessDefinition("Hiring", "Hiring workflow")
        );

        ProcessDefinition result = processDefinitionService.getProcess(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo("Hiring");
    }

    @Test
    @DisplayName("getProcess должен выбрасывать исключение, если процесс не найден")
    void getProcess_shouldThrowWhenProcessNotFound() {
        assertThatThrownBy(() -> processDefinitionService.getProcess(99999L))
                .isInstanceOf(ProcessDefinitionNotFoundException.class)
                .hasMessageContaining("не найдено");
    }

    @Test
    @DisplayName("updateProcess должен обновлять существующее определение процесса")
    void updateProcess_shouldUpdateExistingProcessDefinition() {
        ProcessDefinition saved = processDefinitionRepository.save(
                new ProcessDefinition("Old title", "Old description")
        );

        saved.setTitle("New title");
        saved.setDescription("New description");
        saved.setCategory("Updated category");

        ProcessDefinition updated = processDefinitionService.updateProcess(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getCategory()).isEqualTo("Updated category");
    }

    @Test
    @DisplayName("updateProcess должен выбрасывать исключение, если id отсутствует")
    void updateProcess_shouldThrowWhenIdIsNull() {
        ProcessDefinition definition = new ProcessDefinition("Draft", "Draft description");

        assertThatThrownBy(() -> processDefinitionService.updateProcess(definition))
                .isInstanceOf(ProcessDefinitionValidationException.class)
                .hasMessageContaining("без идентификатора");
    }

    @Test
    @DisplayName("deleteProcess должен удалять определение процесса")
    void deleteProcess_shouldDeleteProcessDefinition() {
        ProcessDefinition saved = processDefinitionRepository.save(
                new ProcessDefinition("To delete", "Delete me")
        );

        processDefinitionService.deleteProcess(saved.getId());

        assertThat(processDefinitionRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("getProcessWithSteps должен возвращать процесс вместе с шагами")
    void getProcessWithSteps_shouldReturnProcessWithSteps() {
        ProcessDefinition definition = new ProcessDefinition("Document Flow", "Document flow process");
        definition.setCategory("Docs");

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        StepDefinition review = new StepDefinition("Review", StepType.USER_TASK);

        definition.addStep(start);
        definition.addStep(review);

        ProcessDefinition saved = processDefinitionRepository.save(definition);

        ProcessDefinition result = processDefinitionService.getProcessWithSteps(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getSteps()).hasSize(2);
        assertThat(result.getSteps())
                .extracting(StepDefinition::getName)
                .containsExactlyInAnyOrder("Start", "Review");
    }

    @Test
    @DisplayName("addStep должен добавлять шаг в определение процесса")
    void addStep_shouldAddStepToProcessDefinition() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Approval", "Approval process")
        );

        StepDefinition created = processDefinitionService.addStep(
                definition.getId(),
                "Approve request",
                StepType.USER_TASK
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Approve request");
        assertThat(created.getType()).isEqualTo(StepType.USER_TASK);
        assertThat(created.getProcessDefinition()).isNotNull();
        assertThat(created.getProcessDefinition().getId()).isEqualTo(definition.getId());
    }

    @Test
    @DisplayName("getSteps должен возвращать все шаги процесса")
    void getSteps_shouldReturnAllStepsOfProcessDefinition() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Vacation request", "Vacation request workflow")
        );

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        start.setProcessDefinition(definition);
        stepDefinitionRepository.save(start);

        StepDefinition approve = new StepDefinition("Approve", StepType.USER_TASK);
        approve.setProcessDefinition(definition);
        stepDefinitionRepository.save(approve);

        List<StepDefinition> steps = processDefinitionService.getSteps(definition.getId());

        assertThat(steps).hasSize(2);
        assertThat(steps)
                .extracting(StepDefinition::getName)
                .containsExactlyInAnyOrder("Start", "Approve");
    }

    @Test
    @DisplayName("addTransition должен добавлять переход между шагами одного процесса")
    void addTransition_shouldAddTransitionBetweenStepsOfSameProcessDefinition() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Expense process", "Expense process")
        );

        StepDefinition fromStep = new StepDefinition("Start", StepType.START_EVENT);
        fromStep.setProcessDefinition(definition);
        fromStep = stepDefinitionRepository.save(fromStep);

        StepDefinition toStep = new StepDefinition("Approve", StepType.USER_TASK);
        toStep.setProcessDefinition(definition);
        toStep = stepDefinitionRepository.save(toStep);

        Transition transition = processDefinitionService.addTransition(
                definition.getId(),
                fromStep.getId(),
                toStep.getId(),
                "start_to_approve",
                "amount > 0"
        );

        assertThat(transition.getId()).isNotNull();
        assertThat(transition.getName()).isEqualTo("start_to_approve");
        assertThat(transition.getCondition()).isEqualTo("amount > 0");
        assertThat(transition.getFromStep().getId()).isEqualTo(fromStep.getId());
        assertThat(transition.getToStep().getId()).isEqualTo(toStep.getId());
        assertThat(transition.getProcessDefinition().getId()).isEqualTo(definition.getId());
    }

    @Test
    @DisplayName("addTransition должен выбрасывать исключение, если шаг не принадлежит определению процесса")
    void addTransition_shouldThrowWhenStepBelongsToAnotherProcessDefinition() {
        ProcessDefinition firstDefinition = processDefinitionRepository.save(
                new ProcessDefinition("Main process", "Main process")
        );

        ProcessDefinition secondDefinition = processDefinitionRepository.save(
                new ProcessDefinition("Foreign process", "Foreign process")
        );

        StepDefinition fromStep = new StepDefinition("Start", StepType.START_EVENT);
        fromStep.setProcessDefinition(firstDefinition);
        fromStep = stepDefinitionRepository.save(fromStep);

        StepDefinition foreignStep = new StepDefinition("Foreign", StepType.USER_TASK);
        foreignStep.setProcessDefinition(secondDefinition);
        foreignStep = stepDefinitionRepository.save(foreignStep);

        StepDefinition finalForeignStep = foreignStep;
        StepDefinition finalFromStep = fromStep;

        assertThatThrownBy(() -> processDefinitionService.addTransition(
                firstDefinition.getId(),
                finalFromStep.getId(),
                finalForeignStep.getId(),
                "invalid_transition",
                "x > 0"
        ))
                .isInstanceOf(StepDoesNotBelongToProcessDefinitionException.class)
                .hasMessageContaining("не принадлежит определению процесса");
    }


    @Test
    @DisplayName("addStepWithTransitions должен выбрасывать исключение, если переход null")
    void addStepWithTransitions_shouldThrowWhenTransitionIsNull() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Null transition process", "Null transition process")
        );

        assertThatThrownBy(() -> processDefinitionService.addStepWithTransitions(
                definition.getId(),
                "Approval",
                StepType.USER_TASK,
                null
        ))
                .isInstanceOf(TransitionValidationException.class);
    }

    @Test
    @DisplayName("addTransition должен выбрасывать исключение, если fromStep не найден")
    void addTransition_shouldThrowWhenFromStepNotFound() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Process", "Process")
        );

        StepDefinition toStep = new StepDefinition("End", StepType.END_EVENT);
        toStep.setProcessDefinition(definition);
        toStep = stepDefinitionRepository.save(toStep);

        StepDefinition finalToStep = toStep;
        assertThatThrownBy(() -> processDefinitionService.addTransition(
                definition.getId(),
                999999L,
                finalToStep.getId(),
                "invalid",
                null
        ))
                .isInstanceOf(StepNotFoundException.class)
                .hasMessageContaining("Шаг-источник");
    }

    @Test
    @DisplayName("getTransitions должен возвращать переходы процесса")
    void getTransitions_shouldReturnTransitionsOfProcessDefinition() {
        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Transition process", "Transition process")
        );

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        start.setProcessDefinition(definition);
        start = stepDefinitionRepository.save(start);

        StepDefinition end = new StepDefinition("End", StepType.END_EVENT);
        end.setProcessDefinition(definition);
        end = stepDefinitionRepository.save(end);

        Transition transition = new Transition(end, start, "approved");
        transition.setName("start_to_end");
        transition.setFromStep(start);
        transition.setProcessDefinition(definition);
        transitionRepository.save(transition);

        List<Transition> transitions = processDefinitionService.getTransitions(definition.getId());

        assertThat(transitions).hasSize(1);
        assertThat(transitions.get(0).getName()).isEqualTo("start_to_end");
    }
}