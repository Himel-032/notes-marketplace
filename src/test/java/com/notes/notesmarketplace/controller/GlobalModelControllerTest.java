package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalModelControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Test
    void addCurrentUserName_shouldAddNameWhenAuthenticatedUserExists() {
        GlobalModelController controller = new GlobalModelController(userRepository);

        User user = new User();
        user.setName("Ariyan");
        user.setEmail("buyer@mail.com");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "buyer@mail.com",
            "n/a",
            List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
        );

        when(userRepository.findByEmail("buyer@mail.com")).thenReturn(Optional.of(user));

        controller.addCurrentUserName(model, authentication);

        verify(model).addAttribute("currentUserName", "Ariyan");
    }

    @Test
    void addCurrentUserName_shouldSkipWhenAnonymous() {
        GlobalModelController controller = new GlobalModelController(userRepository);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "anonymousUser",
            "n/a",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        controller.addCurrentUserName(model, authentication);

        verify(userRepository, never()).findByEmail("anonymousUser");
        verify(model, never()).addAttribute("currentUserName", "anonymousUser");
    }
}
