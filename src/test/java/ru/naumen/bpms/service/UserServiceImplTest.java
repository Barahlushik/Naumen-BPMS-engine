package ru.naumen.bpms.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.repository.UserRepository;
import ru.naumen.bpms.service.exception.user.UserAlreadyExistException;
import ru.naumen.bpms.service.exception.user.UserNotFoundException;
import ru.naumen.bpms.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("createUser должен создать пользователя с закодированным паролем")
    void createUser_shouldCreateUserWithEncodedPassword() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("RawPassword123")).thenReturn("$2a$encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(
                "john",
                "John Smith",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "RawPassword123"
        );

        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getDisplayName()).isEqualTo("John Smith");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getPasswordHash()).isEqualTo("$2a$encoded");

        verify(passwordEncoder).encode("RawPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser должен выбросить исключение, если username уже существует")
    void createUser_shouldThrowWhenUsernameAlreadyExists() {
        User existing = new User(
                "john",
                "John Smith",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "$2a$encoded"
        );

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.createUser(
                "john",
                "John Smith",
                "john2@example.com",
                UserRole.ROLE_USER,
                true,
                "RawPassword123"
        ))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("username john");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("createUser должен выбросить исключение, если email уже существует")
    void createUser_shouldThrowWhenEmailAlreadyExists() {
        User existing = new User(
                "existing",
                "Existing User",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "$2a$encoded"
        );

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.createUser(
                "john",
                "John Smith",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "RawPassword123"
        ))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("email john@example.com");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("getUserById должен вернуть пользователя")
    void getUserById_shouldReturnUser() {
        User user = new User(
                "john",
                "John Smith",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "$2a$encoded"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.getUserById(1L);
        assertThat(result.getUsername()).isEqualTo("john");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById должен выбросить исключение, если пользователь не найден")
    void getUserById_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id=999");

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("getUserByUsername должен вернуть активного пользователя")
    void getUserByUsername_shouldReturnActiveUser() {
        User user = new User(
                "john",
                "John Smith",
                "john@example.com",
                UserRole.ROLE_USER,
                true,
                "$2a$encoded"
        );

        when(userRepository.findByUsernameAndActiveTrue("john")).thenReturn(Optional.of(user));
        User result = userService.getUserByUsername("john");
        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("getUserByUsername должен выбросить исключение, если активный пользователь не найден")
    void getUserByUsername_shouldThrowWhenActiveUserNotFound() {
        when(userRepository.findByUsernameAndActiveTrue("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("missing"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
