package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelController {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addCurrentUserName(Model model, Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            userRepository.findByEmail(authentication.getName())
                    .ifPresent(user -> model.addAttribute("currentUserName", user.getName()));
        }
    }
}