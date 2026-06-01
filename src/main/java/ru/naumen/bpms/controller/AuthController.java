package ru.naumen.bpms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.naumen.bpms.controller.dto.UserDto;
import ru.naumen.bpms.model.UserRole;
import ru.naumen.bpms.service.UserService;
import ru.naumen.bpms.service.exception.user.UserAlreadyExistException;

@Controller
@Slf4j
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        log.info("Registration page requested.");
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") UserDto dto, Model model) {
        log.info("User registration requested. username={}, email={}", dto.getUsername(), dto.getEmail());
        try {
            userService.createUser(
                    dto.getUsername(),
                    dto.getDisplayName(),
                    dto.getEmail(),
                    UserRole.ROLE_USER,
                    true,
                    dto.getPassword()
            );

            log.info("User registration completed. username={}", dto.getUsername());
            return "redirect:/login";

        } catch (UserAlreadyExistException ex) {
            log.warn("User registration rejected. username={}, email={}, message={}",
                    dto.getUsername(), dto.getEmail(), ex.getMessage());
            model.addAttribute("registrationError", ex.getMessage());
            model.addAttribute("user", dto);
            return "register";

        } catch (Exception ex) {
            log.error("User registration failed unexpectedly. username={}, email={}",
                    dto.getUsername(), dto.getEmail(), ex);
            model.addAttribute("registrationError", "Ошибка регистрации: " + ex.getMessage());
            model.addAttribute("user", dto);
            return "register";
        }
    }
    @GetMapping("/login")
    public String login() {
        log.info("Login page requested.");
        return "login";
    }
}
