package ru.naumen.bpms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.repository.UserRepository;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> {
                    log.warn("Active user was not found during authentication. username={}", username);
                    return new UsernameNotFoundException(username);
                });

        log.debug("Active user loaded for authentication. userId={}, username={}", user.getId(), user.getUsername());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .disabled(!user.isActive())
                .build();
    }
}
