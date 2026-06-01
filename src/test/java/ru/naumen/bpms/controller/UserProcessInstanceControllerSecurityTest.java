package ru.naumen.bpms.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;
import ru.naumen.bpms.model.*;
import ru.naumen.bpms.service.ProcessInstanceService;
import ru.naumen.bpms.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserProcessInstanceControllerSecurityTest {

    @Test
    @DisplayName("пользователь не может завершить чужой экземпляр процесса")
    void completeProcess_shouldRejectNonOwnerUser() {
        User owner = user(1L, "owner");
        User requester = user(2L, "requester");
        ProcessInstance processInstance = processInstance(owner);

        UserProcessInstanceController controller = new UserProcessInstanceController(
                new StubProcessInstanceService(processInstance),
                new StubUserService(requester),
                null,
                null
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                requester.getUsername(),
                "n/a",
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );

        assertThatThrownBy(() -> controller.completeProcess(processInstance.getId(), authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа");
    }

    private User user(Long id, String username) {
        User user = new User(username, username, username + "@test.local", UserRole.ROLE_USER, true, "passwordHash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ProcessInstance processInstance(User owner) {
        ProcessDefinition definition = new ProcessDefinition("Test process", "Test process");
        ReflectionTestUtils.setField(definition, "id", 10L);

        StepDefinition start = new StepDefinition("Start", StepType.START_EVENT);
        ReflectionTestUtils.setField(start, "id", 20L);
        start.setProcessDefinition(definition);

        ProcessInstance processInstance = new ProcessInstance(definition, owner, start);
        ReflectionTestUtils.setField(processInstance, "id", 100L);
        return processInstance;
    }

    private static class StubUserService implements UserService {
        private final User currentUser;

        private StubUserService(User currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        public User createUser(String username, String displayName, String email, UserRole role, boolean active, String rawPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User getUserById(Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User getUserByUsername(String username) {
            return currentUser;
        }

        @Override
        public List<User> getAllUsers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public User updateUser(Long userId, String username, String displayName, String email, UserRole role, boolean active) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteUser(Long userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class StubProcessInstanceService implements ProcessInstanceService {
        private final ProcessInstance processInstance;

        private StubProcessInstanceService(ProcessInstance processInstance) {
            this.processInstance = processInstance;
        }

        @Override
        public ProcessInstance createProcess(ProcessInstance process) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance getProcess(Long id) {
            return processInstance;
        }

        @Override
        public List<ProcessInstance> getAllProcesses() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance updateProcess(ProcessInstance process) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteProcess(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance startProcess(Long processDefinitionId, Long ownerId, Long startStepId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ProcessInstance> getProcessesByOwnerAndStatus(Long ownerId, ProcessStatus status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ProcessInstance> getProcessesByOwner(Long ownerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Transition> getAvailableTransitions(Long processInstanceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance moveToStep(Long processInstanceId, Long nextStepId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance executeTransition(Long processInstanceId, Long transitionId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance completeProcess(Long processInstanceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance cancelProcess(Long processInstanceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance addParticipant(Long processInstanceId, Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProcessInstance removeParticipant(Long processInstanceId, Long userId) {
            throw new UnsupportedOperationException();
        }
    }
}

