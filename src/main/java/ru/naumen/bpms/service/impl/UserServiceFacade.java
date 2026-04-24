package ru.naumen.bpms.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.repository.UserRepository;
import ru.naumen.bpms.service.UserService;
import ru.naumen.bpms.service.exception.user.UserAlreadyExistException;
import ru.naumen.bpms.service.exception.user.UserNotFoundException;

@Service
@Validated
public class UserServiceFacade implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceFacade(UserRepository userRepository,
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
            throw new UserAlreadyExistException(String.format("Пользователь c username %s уже существует", username));
        }
        if (userRepository.findByEmail(email).isPresent()) {
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

        return userRepository.save(user);

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

        return userRepository.save(user);
    }


}
