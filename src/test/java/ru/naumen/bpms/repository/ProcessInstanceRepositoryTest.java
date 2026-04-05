package ru.naumen.bpms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProcessInstanceRepositoryTest {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByOwnerIdAndStatus должен возвращать процессы владельца в заданном статусе")
    void findByOwnerIdAndStatus_shouldReturnMatchingProcesses() {
        User owner = userRepository.save(
                new User(
                        "johnsmith",
                        "John Smith",
                        "john@example.com",
                        UserRole.EMPLOYEE,
                        true,
                        "StrongPassword"
                )
        );

        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Leave Request", "Leave request process")
        );

        StepDefinition startStep = new StepDefinition("Start", StepType.START_EVENT);
        startStep.setProcessDefinition(definition);
        startStep = stepDefinitionRepository.save(startStep);

        ProcessInstance runningInstance = new ProcessInstance(definition, owner, startStep);
        processInstanceRepository.save(runningInstance);

        ProcessInstance completedInstance = new ProcessInstance(definition, owner, startStep);
        completedInstance.complete();
        processInstanceRepository.save(completedInstance);

        List<ProcessInstance> result =
                processInstanceRepository.findByOwnerIdAndStatus(owner.getId(), ProcessStatus.RUNNING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwner().getId()).isEqualTo(owner.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ProcessStatus.RUNNING);
    }

    @Test
    @DisplayName("findByOwnerIdAndStatus должен возвращать пустой список, если совпадений нет")
    void findByOwnerIdAndStatus_shouldReturnEmptyListWhenNoMatches() {
        User owner = userRepository.save(
                new User(
                        "janesmith",
                        "Jane Smith",
                        "jane@example.com",
                        UserRole.EMPLOYEE,
                        true,
                        "AnotherStrong"
                )
        );

        List<ProcessInstance> result =
                processInstanceRepository.findByOwnerIdAndStatus(owner.getId(), ProcessStatus.CANCELLED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findWithParticipantsById должен возвращать процесс вместе с participants")
    void findWithParticipantsById_shouldReturnProcessWithParticipants() {
        User owner = userRepository.save(
                new User(
                        "owneruser",
                        "Owner User",
                        "owner@example.com",
                        UserRole.EMPLOYEE,
                        true,
                        "StrongPassword"
                )
        );

        User participant = userRepository.save(
                new User(
                        "participantuser",
                        "Participant User",
                        "participant@example.com",
                        UserRole.EMPLOYEE,
                        true,
                        "AnotherStrong"
                )
        );

        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Expense Approval", "Expense approval process")
        );

        StepDefinition currentStep = new StepDefinition("Approval", StepType.USER_TASK);
        currentStep.setProcessDefinition(definition);
        currentStep = stepDefinitionRepository.save(currentStep);

        ProcessInstance instance = new ProcessInstance(definition, owner, currentStep);
        instance.addParticipant(participant);

        ProcessInstance savedInstance = processInstanceRepository.save(instance);

        Optional<ProcessInstance> result =
                processInstanceRepository.findWithParticipantsById(savedInstance.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedInstance.getId());
        assertThat(result.get().getParticipants()).hasSize(2);
        assertThat(result.get().getParticipants())
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("owneruser", "participantuser");
    }

    @Test
    @DisplayName("findWithParticipantsById должен возвращать empty, если процесс не найден")
    void findWithParticipantsById_shouldReturnEmptyWhenNotFound() {
        Optional<ProcessInstance> result =
                processInstanceRepository.findWithParticipantsById(999999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findDetailedById должен возвращать процесс с participants, currentStep и processDefinition")
    void findDetailedById_shouldReturnDetailedProcessInstance() {
        User owner = userRepository.save(
                new User(
                        "alexuser",
                        "Alex User",
                        "alex@example.com",
                        UserRole.EMPLOYEE,
                        true,
                        "VeryStrongPwd"
                )
        );

        User participant = userRepository.save(
                new User(
                        "revieweruser",
                        "Reviewer User",
                        "reviewer@example.com",
                        UserRole.ADMIN,
                        true,
                        "PasswordStrong"
                )
        );

        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Contract Approval", "Contract approval workflow")
        );
        definition.setCategory("Legal");
        definition = processDefinitionRepository.save(definition);

        StepDefinition currentStep = new StepDefinition("Review Contract", StepType.USER_TASK);
        currentStep.setProcessDefinition(definition);
        currentStep = stepDefinitionRepository.save(currentStep);

        ProcessInstance instance = new ProcessInstance(definition, owner, currentStep);
        instance.addParticipant(participant);

        ProcessInstance savedInstance = processInstanceRepository.save(instance);

        Optional<ProcessInstance> result =
                processInstanceRepository.findDetailedById(savedInstance.getId());

        assertThat(result).isPresent();

        ProcessInstance loaded = result.get();

        assertThat(loaded.getId()).isEqualTo(savedInstance.getId());
        assertThat(loaded.getParticipants()).hasSize(2);
        assertThat(loaded.getCurrentStep()).isNotNull();
        assertThat(loaded.getCurrentStep().getId()).isEqualTo(currentStep.getId());
        assertThat(loaded.getCurrentStep().getName()).isEqualTo("Review Contract");
        assertThat(loaded.getProcessDefinition()).isNotNull();
        assertThat(loaded.getProcessDefinition().getId()).isEqualTo(definition.getId());
        assertThat(loaded.getProcessDefinition().getTitle()).isEqualTo("Contract Approval");
    }

    @Test
    @DisplayName("findDetailedById должен возвращать empty, если процесс не найден")
    void findDetailedById_shouldReturnEmptyWhenNotFound() {
        Optional<ProcessInstance> result =
                processInstanceRepository.findDetailedById(123456L);

        assertThat(result).isEmpty();
    }
}
