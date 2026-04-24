package ru.naumen.bpms.controller;

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
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") UserDto dto, Model model) {
        try {
            userService.createUser(
                    dto.getUsername(),
                    dto.getDisplayName(),
                    dto.getEmail(),
                    UserRole.ROLE_USER,
                    true,
                    dto.getPassword()
            );

            return "redirect:/login";

        } catch (UserAlreadyExistException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            model.addAttribute("user", dto);
            return "register";

        } catch (Exception ex) {
            model.addAttribute("registrationError", "Ошибка регистрации: " + ex.getMessage());
            model.addAttribute("user", dto);
            return "register";
        }
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}