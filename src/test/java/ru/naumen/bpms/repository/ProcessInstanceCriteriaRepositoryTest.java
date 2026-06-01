package ru.naumen.bpms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.*;
import ru.naumen.bpms.repository.criteria.impl.ProcessInstanceRepositoryImpl;
import ru.naumen.bpms.testsupport.PostgreSqlTestContainerSupport;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProcessInstanceRepositoryImpl.class)
class ProcessInstanceCriteriaRepositoryTest extends PostgreSqlTestContainerSupport {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByOwnerIdAndStatusCriteria должен возвращать процессы владельца с заданным статусом")
    void findByOwnerIdAndStatusCriteria_shouldReturnMatchingProcesses() {

        // создаем пользователя (1 условие)
        User owner = userRepository.save(
                new User(
                        "johnsmith",
                        "John Smith",
                        "john@example.com",
                        UserRole.ROLE_USER,
                        true,
                        "StrongPassword"
                )
        );


        // сохраняем мету процесса
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
                processInstanceRepository.findByOwnerIdAndStatusCriteria(owner.getId(), ProcessStatus.RUNNING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwner().getId()).isEqualTo(owner.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(ProcessStatus.RUNNING);
    }

    @Test
    @DisplayName("findByOwnerIdAndStatusCriteria должен возвращать пустой список, если совпадений нет")
    void findByOwnerIdAndStatusCriteria_shouldReturnEmptyListWhenNoMatches() {
        User owner = userRepository.save(
                new User(
                        "janesmith",
                        "Jane Smith",
                        "jane@example.com",
                        UserRole.ROLE_USER,
                        true,
                        "AnotherStrong"
                )
        );

        List<ProcessInstance> result =
                processInstanceRepository.findByOwnerIdAndStatusCriteria(owner.getId(), ProcessStatus.CANCELLED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCurrentStepIdAndProcessDefinitionIdCriteria должен возвращать процесс по шагу и определению процесса")
    void findByCurrentStepIdAndProcessDefinitionIdCriteria_shouldReturnMatchingProcess() {
        User owner = userRepository.save(
                new User(
                        "alexuser",
                        "Alex User",
                        "alex@example.com",
                        UserRole.ROLE_USER,
                        true,
                        "StrongPassWord"
                )
        );

        ProcessDefinition definition = processDefinitionRepository.save(
                new ProcessDefinition("Expense Approval", "Expense approval process")
        );

        StepDefinition currentStep = new StepDefinition("Approval", StepType.USER_TASK);
        currentStep.setProcessDefinition(definition);
        currentStep = stepDefinitionRepository.save(currentStep);

        ProcessInstance instance = new ProcessInstance(definition, owner, currentStep);
        processInstanceRepository.save(instance);

        Optional<ProcessInstance> result =
                processInstanceRepository.findByCurrentStepIdAndProcessDefinitionIdCriteria(
                        currentStep.getId(),
                        definition.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getCurrentStep().getId()).isEqualTo(currentStep.getId());
        assertThat(result.get().getProcessDefinition().getId()).isEqualTo(definition.getId());
    }

    @Test
    @DisplayName("findByCurrentStepIdAndProcessDefinitionIdCriteria должен возвращать empty, если процесс не найден")
    void findByCurrentStepIdAndProcessDefinitionIdCriteria_shouldReturnEmptyWhenNoMatches() {
        Optional<ProcessInstance> result =
                processInstanceRepository.findByCurrentStepIdAndProcessDefinitionIdCriteria(99999L, 88888L);

        assertThat(result).isEmpty();
    }
}
