package com.example.group56.controller;

import com.example.group56.dto.AccountDTO;
import com.example.group56.dto.UserDTO;
import com.example.group56.model.User;
import com.example.group56.service.UserService;
import com.example.group56.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// Mappings summary:
// /settings                        - returns template with all user fields except password
// /settings/update                 - validates then updates user's settings in database with field values
// /settings/reset-password (GET)   - returns template for updating user password
// /settings/reset-password (POST)  - validates then updates user's password in database
// /logout (GET)                    - logs out the user
// /login (GET)                     - returns login template
// /register (GET)                  - returns registration template
// /register (POST)                 - validates then create user with details
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/settings")
    public String getSettingsPage(@AuthenticationPrincipal User user, Model model) {
        if(!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO(user));
        }

        return "settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@AuthenticationPrincipal User user,
                                 @Valid @ModelAttribute("userDTO") UserDTO userDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        // Check if field values match internal validation
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userDTO", result);
            redirectAttributes.addFlashAttribute("userDTO", userDTO);
            return "redirect:/settings";
        }

        // Track if any changes are made before attempting to update database
        boolean changes = false;

        // Update values of entity
        if (!user.getEmail().equals(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
            changes = true;
        }
        if (!user.getBio().equals(userDTO.getBio())) {
            user.setBio(userDTO.getBio());
            changes = true;
        }
        if (!user.getName().equals(userDTO.getName())) {
            user.setName(userDTO.getName());
            changes = true;
        }

        if (changes) {
            // Update database
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "No changes made.");
        }
        return "redirect:/settings";
    }

    // Displays Reset Password template
    @GetMapping("/settings/reset-password")
    public String showResetPassword(@AuthenticationPrincipal User user,
                                    Model model) {
        // Add blank account if not already added to model
        if(!model.containsAttribute("accountDTO")) {
            model.addAttribute("accountDTO", new AccountDTO(user));
        }
        return "reset-password";
    }

    // Performs checks then updates user's password with encrypted input
    @PostMapping("/settings/reset-password")
    public String resetPassword(
                                @Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                                BindingResult result,
                                @RequestParam String currentPassword,
                                @RequestParam String newPasswordConfirm,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                HttpServletResponse response
                                ) {
        boolean hasErrors = false;
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("incorrectPasswordError", "Incorrect current password.");
            hasErrors = true;
        }

        // Internal validation errors which check if password is valid
        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.accountDTO", result);
            redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
            hasErrors = true;
        }

        // Ensure both new password entries match
        if (!accountDTO.getPassword().equals(newPasswordConfirm)) {
            redirectAttributes.addFlashAttribute("notMatchingError", "New password entries do not match.");
            hasErrors = true;
        }

        // Only redirect after all errors have been caught
        if (hasErrors) {
            return "redirect:/settings/reset-password";
        }

        // Check that password is new before updating database.
        if (accountDTO.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("successMessage", "New password is same as before.");
            return "redirect:/settings/reset-password";
        }

        // Hash and save the new password securely
        user.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully.");

        return logout(request, response, redirectAttributes);
    }

    // Logs out the user. GET request for browser access.
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (!redirectAttributes.getFlashAttributes().containsKey("successMessage")) {
            redirectAttributes.addFlashAttribute("successMessage", "Logout successful.");
        }
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }

    // Returns the login template
    @GetMapping("/login")
    public String login(Principal principal, RedirectAttributes redirectAttributes, @RequestParam(required = false) String error) {
        if (principal == null) {
            if (error != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Incorrect email or password.");
                return "redirect:/login";
            }
            return "login";
        } else {
            return "redirect:/";
        }
    }

    // Returns the registration template
    @GetMapping("/register")
    public String showRegisterForm(Principal principal, Model model) {
        if (principal == null) {
            if (!model.containsAttribute("accountDTO")) {
                model.addAttribute("accountDTO", new AccountDTO());
            }
            return "register";
        } else {
            return "redirect:/";
        }
    }

    // Validates registration inputs then creates a new user in database.
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute AccountDTO accountDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.accountDTO", result);
            redirectAttributes.addFlashAttribute("accountDTO", accountDTO);
            return "redirect:/register";
        }

        try {
            userService.registerUser(accountDTO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Registered successfully.");
        return "redirect:/login";
    }
}
