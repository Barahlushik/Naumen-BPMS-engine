package ru.naumen.bpms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.repository.UserRepository;
import ru.naumen.bpms.service.UserService;
import ru.naumen.bpms.service.exception.user.UserAlreadyExistException;
import ru.naumen.bpms.service.exception.user.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String username,
                           String displayName,
                           String email,
                           UserRole role,
                           boolean active,
                           String rawPassword) {

        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("User creation rejected: username already exists. username={}", username);
            throw new UserAlreadyExistException(String.format("Пользователь c username %s уже существует", username));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("User creation rejected: email already exists. email={}", email);
            throw new UserAlreadyExistException(String.format("Пользователь c email %s уже существует", email));
        }

        User user = new User(
                username,
                displayName,
                email,
                role,
                active,
                null
        );

        user.changePassword(passwordEncoder.encode(rawPassword));

        User savedUser = userRepository.save(user);
        log.info("User created. userId={}, username={}, role={}, active={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole(), savedUser.isActive());

        return savedUser;

    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%d не найден.", id)));
    }

    @Override
    public User getUserByUsername(String uname) {
        return userRepository.findByUsernameAndActiveTrue(uname)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с username = %s не найден.", uname)));
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        log.info("Users loaded. usersCount={}", users.size());
        return users;
    }


    @Override
    public User updateUser(Long id,
                           String uname,
                           String displayName,
                           String email,
                           UserRole role,
                           boolean active) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%d не найден.", id)));

        user.setUsername(uname);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);

        User savedUser = userRepository.save(user);
        log.info("User updated. userId={}, username={}, role={}, active={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole(), savedUser.isActive());

        return savedUser;
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с id=%d не найден.", id)));

        userRepository.delete(user);
        log.info("User deleted. userId={}, username={}", user.getId(), user.getUsername());
    }

}
