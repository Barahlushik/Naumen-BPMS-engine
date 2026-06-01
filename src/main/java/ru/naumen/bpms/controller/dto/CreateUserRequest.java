package ru.naumen.bpms.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.naumen.bpms.model.UserRole;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Size(min = 2, max = 100)
        String displayName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotNull
        UserRole role,

        boolean active,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}

