package ru.naumen.bpms.service;

import ru.naumen.bpms.model.User;
import ru.naumen.bpms.model.UserRole;

import java.util.List;

public interface UserService {

    User createUser(String username,
                    String displayName,
                    String email,
                    UserRole role,
                    boolean active,
                    String rawPassword);

    User getUserById(Long userId);

    User getUserByUsername(String username);

    List<User> getAllUsers();

    User updateUser(Long userId,
                    String username,
                    String displayName,
                    String email,
                    UserRole role,
                    boolean active);

    void deleteUser(Long userId);

}
