package ru.naumen.bpms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.*;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;
import ru.naumen.bpms.repository.ProcessInstanceRepository;
import ru.naumen.bpms.repository.StepDefinitionRepository;
import ru.naumen.bpms.repository.UserRepository;
import ru.naumen.bpms.service.exception.process.ProcessInstanceException;
import ru.naumen.bpms.service.impl.ProcessInstanceServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ProcessInstanceServiceImplIntegrationTest {

    @Autowired
    private ProcessInstanceServiceImpl processInstanceService;

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Autowired
    private ProcessDefinitionRepository processDefinitionRepository;

    @Autowired
    private StepDefinitionRepository stepDefinitionRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        processInstanceRepository.deleteAll();
        stepDefinitionRepository.deleteAll();
        processDefinitionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("startProcess должен сохранить экземпляр процесса и участников в рамках одной транзакции")
    void startProcess_shouldPersistProcessInstanceAndParticipants() {

        User owner = userRepository.save(
                new User("owner_user", "Owner User", "owner@test.local",
                        UserRole.EMPLOYEE, true, "StrongPassword")
        );

        ProcessDefinition definition = new ProcessDefinition("Leave Request", "Leave request process");
        definition.setCategory("HR");
        definition = processDefinitionRepository.save(definition);

        StepDefinition startStep = new StepDefinition("Start", StepType.START_EVENT);
        startStep.setProcessDefinition(definition);
        startStep = stepDefinitionRepository.save(startStep);

        ProcessInstance created = processInstanceService.startProcess(
                definition.getId(),
                owner.getId(),
                startStep.getId()
        );

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ProcessStatus.RUNNING);
        assertThat(created.getProcessDefinition().getId()).isEqualTo(definition.getId());
        assertThat(created.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(created.getCurrentStep().getId()).isEqualTo(startStep.getId());

        ProcessInstance persisted = processInstanceRepository.findDetailedById(created.getId()).orElseThrow();

        assertThat(persisted.getParticipants())
                .extracting(User::getId)
                .containsExactlyInAnyOrder(owner.getId());
    }

    @Test
    @DisplayName("startProcess должен откатывать транзакцию, если вторая фаза операции завершается ошибкой")
    void startProcess_shouldRollbackWhenParticipantFinalizationFails() {

        User owner = userRepository.save(
                new User("non-active_owner", "Non-active Owner", "non-active_owner@test.local",
                        UserRole.EMPLOYEE, false, "StrongPassword")
        );

        ProcessDefinition definition = new ProcessDefinition("Expense Approval", "Expense approval process");
        definition.setCategory("Finance");
        definition = processDefinitionRepository.save(definition);

        StepDefinition startStep = new StepDefinition("Start", StepType.START_EVENT);
        startStep.setProcessDefinition(definition);
        startStep = stepDefinitionRepository.save(startStep);

        long beforeCount = processInstanceRepository.count();

        ProcessDefinition finalDefinition = definition;
        StepDefinition finalStartStep = startStep;
        assertThatThrownBy(() -> processInstanceService.startProcess(
                finalDefinition.getId(),
                owner.getId(),
                finalStartStep.getId()
        ))
                .isInstanceOf(ProcessInstanceException.class);

        long afterCount = processInstanceRepository.count();

        assertThat(afterCount).isEqualTo(beforeCount);
        assertThat(processInstanceRepository.findAll()).isEmpty();
    }
}