package ru.naumen.bpms.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.naumen.bpms.controller.dto.ProcessValidationResponseDto;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.model.Transition;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.service.exception.process.ProcessDefinitionValidationException;
import ru.naumen.bpms.service.impl.ProcessDefinitionValidationServiceImpl;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessDefinitionValidationServiceImplTest {

    @Test
    @DisplayName("validate должен возвращать ошибку для процесса без шагов")
    void validate_shouldReturnErrorWhenProcessHasNoSteps() {
        ProcessDefinition definition = processDefinition(1L, "Empty process");
        ProcessDefinitionValidationService service = validationService(definition);

        ProcessValidationResponseDto result = service.validate(definition.getId());

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Процесс не содержит шагов.");
    }

    @Test
    @DisplayName("validate должен возвращать ошибку, если нет стартового шага")
    void validate_shouldReturnErrorWhenStartStepMissing() {
        ProcessDefinition definition = processDefinition(1L, "No start");
        StepDefinition task = addStep(definition, 10L, "Task", StepType.USER_TASK);
        StepDefinition end = addStep(definition, 11L, "End", StepType.END_EVENT);
        addTransition(definition, 100L, task, end, "task_to_end");

        ProcessValidationResponseDto result = validationService(definition).validate(definition.getId());

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Процесс не содержит стартовый шаг START_EVENT.");
    }

    @Test
    @DisplayName("validate должен возвращать ошибку, если нет конечного шага")
    void validate_shouldReturnErrorWhenEndStepMissing() {
        ProcessDefinition definition = processDefinition(1L, "No end");
        StepDefinition start = addStep(definition, 10L, "Start", StepType.START_EVENT);
        StepDefinition task = addStep(definition, 11L, "Task", StepType.USER_TASK);
        addTransition(definition, 100L, start, task, "start_to_task");

        ProcessValidationResponseDto result = validationService(definition).validate(definition.getId());

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Процесс не содержит конечный шаг END_EVENT.");
    }

    @Test
    @DisplayName("validate должен возвращать ошибку, если конечный шаг недостижим")
    void validate_shouldReturnErrorWhenEndStepIsUnreachable() {
        ProcessDefinition definition = processDefinition(1L, "Unreachable end");
        StepDefinition start = addStep(definition, 10L, "Start", StepType.START_EVENT);
        StepDefinition task = addStep(definition, 11L, "Task", StepType.USER_TASK);
        StepDefinition end = addStep(definition, 12L, "End", StepType.END_EVENT);
        addTransition(definition, 100L, start, task, "start_to_task");

        ProcessValidationResponseDto result = validationService(definition).validate(definition.getId());

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Ни один конечный шаг END_EVENT не достижим из стартового шага.");
        assertThat(result.warnings()).anyMatch(warning -> warning.contains(end.getId().toString()));
    }

    @Test
    @DisplayName("validate должен возвращать предупреждение, если есть недостижимый шаг")
    void validate_shouldReturnWarningWhenStepIsUnreachable() {
        ProcessDefinition definition = processDefinition(1L, "Unreachable task");
        StepDefinition start = addStep(definition, 10L, "Start", StepType.START_EVENT);
        StepDefinition end = addStep(definition, 11L, "End", StepType.END_EVENT);
        StepDefinition unreachableTask = addStep(definition, 12L, "Detached task", StepType.USER_TASK);
        addTransition(definition, 100L, start, end, "start_to_end");

        ProcessValidationResponseDto result = validationService(definition).validate(definition.getId());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
        assertThat(result.warnings()).anyMatch(warning -> warning.contains(unreachableTask.getId().toString()));
    }

    @Test
    @DisplayName("validate должен возвращать valid=true для корректного процесса")
    void validate_shouldReturnValidForCorrectProcess() {
        ProcessDefinition definition = processDefinition(1L, "Valid process");
        StepDefinition start = addStep(definition, 10L, "Start", StepType.START_EVENT);
        StepDefinition task = addStep(definition, 11L, "Task", StepType.USER_TASK);
        StepDefinition end = addStep(definition, 12L, "End", StepType.END_EVENT);
        addTransition(definition, 100L, start, task, "start_to_task");
        addTransition(definition, 101L, task, end, "task_to_end");

        ProcessValidationResponseDto result = validationService(definition).validate(definition.getId());

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("assertValid должен выбрасывать исключение для невалидного процесса")
    void assertValid_shouldThrowWhenProcessIsInvalid() {
        ProcessDefinition definition = processDefinition(1L, "Invalid process");

        assertThatThrownBy(() -> validationService(definition).assertValid(definition.getId()))
                .isInstanceOf(ProcessDefinitionValidationException.class)
                .hasMessageContaining("Определение процесса невалидно");
    }

    private ProcessDefinitionValidationService validationService(ProcessDefinition definition) {
        ProcessDefinitionRepository repository = (ProcessDefinitionRepository) Proxy.newProxyInstance(
                ProcessDefinitionRepository.class.getClassLoader(),
                new Class<?>[]{ProcessDefinitionRepository.class},
                (proxy, method, args) -> {
                    if ("findWithStepsAndTransitionsById".equals(method.getName())) {
                        return Optional.of(definition);
                    }
                    if ("toString".equals(method.getName())) {
                        return "ProcessDefinitionRepositoryStub";
                    }
                    throw new UnsupportedOperationException("Method is not supported by test stub: " + method.getName());
                }
        );

        return new ProcessDefinitionValidationServiceImpl(repository);
    }

    private ProcessDefinition processDefinition(Long id, String title) {
        ProcessDefinition definition = new ProcessDefinition(title, title);
        ReflectionTestUtils.setField(definition, "id", id);
        return definition;
    }

    private StepDefinition addStep(ProcessDefinition definition, Long id, String name, StepType type) {
        StepDefinition step = new StepDefinition(name, type);
        ReflectionTestUtils.setField(step, "id", id);
        definition.addStep(step);
        return step;
    }

    private Transition addTransition(ProcessDefinition definition,
                                     Long id,
                                     StepDefinition fromStep,
                                     StepDefinition toStep,
                                     String name) {
        Transition transition = new Transition(fromStep, toStep, null);
        ReflectionTestUtils.setField(transition, "id", id);
        transition.setName(name);
        transition.setProcessDefinition(definition);
        definition.addTransition(transition);
        return transition;
    }
}

