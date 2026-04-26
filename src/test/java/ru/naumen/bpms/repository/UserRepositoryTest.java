package ru.naumen.bpms.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @jakarta.annotation.Resource
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername должен возвращать пользователя по username")
    void findByUsername_shouldReturnUserWhenExists() {
        User savedUser = userRepository.save(
                new User(
                        "lolik",
                        "kekolik",
                        "marik.kriger@mail.ru",
                        UserRole.ROLE_USER,
                        true,
                        "StrongPassword"
                )
        );

        Optional<User> result = userRepository.findByUsername("lolik");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getUsername()).isEqualTo("lolik");
        assertThat(result.get().getEmail()).isEqualTo("marik.kriger@mail.ru");
    }

    @Test
    @DisplayName("findByUsername должен возвращать empty, если пользователь не найден")
    void findByUsername_shouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userRepository.findByUsername("missing_user");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail должен возвращать пользователя по email")
    void findByEmail_shouldReturnUserWhenExists() {
        User savedUser = userRepository.save(
                new User(
                        "lolik",
                        "kekolik",
                        "marik.kriger@mail.ru",
                        UserRole.ROLE_USER,
                        true,
                        "StrongPassword"
                )
        );

        Optional<User> result = userRepository.findByEmail("marik.kriger@mail.ru");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        assertThat(result.get().getUsername()).isEqualTo("lolik");
        assertThat(result.get().getEmail()).isEqualTo("marik.kriger@mail.ru");
    }

    @Test
    @DisplayName("findByEmail должен возвращать empty, если email не найден")
    void findByEmail_shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> result = userRepository.findByEmail("absent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsernameAndActiveTrue должен возвращать только активного пользователя")
    void findByUsernameAndActiveTrue_shouldReturnActiveUser() {
        userRepository.save(
                new User(
                        "activeuser",
                        "Active User",
                        "active@example.com",
                        UserRole.ROLE_USER,
                        true,
                        "VeryStrongPwd"
                )
        );

        Optional<User> result = userRepository.findByUsernameAndActiveTrue("activeuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("activeuser");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("findByUsernameAndActiveTrue не должен возвращать неактивного пользователя")
    void findByUsernameAndActiveTrue_shouldReturnEmptyForInactiveUser() {
        userRepository.save(
                new User(
                        "inactiveuser",
                        "Inactive User",
                        "inactive@example.com",
                        UserRole.ROLE_USER,
                        false,
                        "VeryStrongPwd"
                )
        );

        Optional<User> result = userRepository.findByUsernameAndActiveTrue("inactiveuser");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsernameAndActiveTrue должен возвращать empty, если username отсутствует")
    void findByUsernameAndActiveTrue_shouldReturnEmptyWhenUsernameDoesNotExist() {
        Optional<User> result = userRepository.findByUsernameAndActiveTrue("unknown_user");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername должен возвращать true, если username существует")
    void existsByUsername_shouldReturnTrueWhenUsernameExists() {
        userRepository.save(
                new User(
                        "existinguser",
                        "Existing User",
                        "existing@example.com",
                        UserRole.ROLE_USER,
                        true,
                        "StrongPassWord"
                )
        );

        boolean exists = userRepository.existsByUsername("existinguser");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername должен возвращать false, если username не существует")
    void existsByUsername_shouldReturnFalseWhenUsernameDoesNotExist() {
        boolean exists = userRepository.existsByUsername("not_existing_user");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmail должен возвращать true, если email существует")
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        userRepository.save(
                new User(
                        "mailuser",
                        "Mail User",
                        "mail@example.com",
                        UserRole.ROLE_ADMIN,
                        true,
                        "PasswordStrong"
                )
        );

        boolean exists = userRepository.existsByEmail("mail@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail должен возвращать false, если email не существует")
    void existsByEmail_shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("missing@example.com");

        assertThat(exists).isFalse();
    }
}