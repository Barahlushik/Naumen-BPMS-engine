package ru.naumen.bpms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.model.StepDefinition;
import ru.naumen.bpms.model.StepType;
import ru.naumen.bpms.testsupport.PostgreSqlTestContainerSupport;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProcessDefinitionRepositoryTest extends PostgreSqlTestContainerSupport {

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private TransitionRepository transitionRepository;

    @Test
    @DisplayName("findWithStepsById должен возвращать process definition вместе со steps")
    void findWithStepsById_shouldReturnDefinitionWithSteps() {
        ProcessDefinition definition = new ProcessDefinition("Leave Request", "Leave request process");
        definition.setCategory("HR");

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        StepDefinition approve = new StepDefinition("Approve", StepType.USER_TASK);

        definition.addStep(start);
        definition.addStep(approve);

        ProcessDefinition saved = processDefinitionRepository.save(definition);

        Optional<ProcessDefinition> result = processDefinitionRepository.findWithStepsById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getSteps()).hasSize(2);
        assertThat(result.get().getSteps())
                .extracting(StepDefinition::getName)
                .containsExactlyInAnyOrder("Start", "Approve");
    }

    @Test
    @DisplayName("findWithStepsById должен возвращать empty, если process definition не существует")
    void findWithStepsById_shouldReturnEmptyWhenDefinitionDoesNotExist() {
        Optional<ProcessDefinition> result = processDefinitionRepository.findWithStepsById(99999L);

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByStepId должен возвращать process definition по id шага")
    void findByStepId_shouldReturnDefinitionByStepId() {
        ProcessDefinition definition = new ProcessDefinition("Vacation Process", "Vacation approval workflow");
        definition.setCategory("HR");

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        StepDefinition review = new StepDefinition("Review", StepType.USER_TASK);

        definition.addStep(start);
        definition.addStep(review);

        ProcessDefinition savedDefinition = processDefinitionRepository.save(definition);

        Long stepId = savedDefinition.getSteps().stream()
                .filter(step -> "Review".equals(step.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        Optional<ProcessDefinition> result = processDefinitionRepository.findByStepId(stepId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedDefinition.getId());
        assertThat(result.get().getTitle()).isEqualTo("Vacation Process");
    }

    @Test
    @DisplayName("findByStepId должен возвращать empty, если шаг не существует")
    void findByStepId_shouldReturnEmptyWhenStepDoesNotExist() {
        Optional<ProcessDefinition> result = processDefinitionRepository.findByStepId(77777L);

        assertThat(result).isEmpty();
    }
}
