package ru.naumen.bpms.controller.dto;

import ru.naumen.bpms.model.UserRole;

public record UserResponseDto(
        Long id,
        String username,
        String displayName,
        String email,
        UserRole role,
        boolean active
) {
}

