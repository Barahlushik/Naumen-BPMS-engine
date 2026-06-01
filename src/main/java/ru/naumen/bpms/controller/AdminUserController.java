package ru.naumen.bpms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.naumen.bpms.controller.dto.CreateUserRequest;
import ru.naumen.bpms.controller.dto.UpdateUserRequest;
import ru.naumen.bpms.controller.dto.UserResponseDto;
import ru.naumen.bpms.controller.mapper.UserMapper;
import ru.naumen.bpms.model.User;
import ru.naumen.bpms.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
@Tag(name = "Admin Users")
@SecurityRequirement(name = "sessionAuth")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserController(UserService userService,
                               UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    public ResponseEntity<List<UserResponseDto>> getUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toDtoList(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по id")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.username(),
                request.displayName(),
                request.email(),
                request.role(),
                request.active(),
                request.password()
        );

        log.info("API response created: user. userId={}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(
                id,
                request.username(),
                request.displayName(),
                request.email(),
                request.role(),
                request.active()
        );

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
