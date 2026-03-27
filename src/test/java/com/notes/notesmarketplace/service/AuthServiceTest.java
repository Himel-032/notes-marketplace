package com.notes.notesmarketplace.service;

import com.notes.notesmarketplace.dto.AuthResponse;
import com.notes.notesmarketplace.dto.LoginRequest;
import com.notes.notesmarketplace.dto.RegisterRequest;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.RoleRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldCreateUser_whenRequestIsValid() {
        RegisterRequest request = TestDataBuilder.registerRequest("John", "john@mail.com", "BUYER");
        Role buyerRole = new Role(2L, "BUYER");

        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("BUYER")).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");

        AuthResponse response = authService.register(request);

        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = TestDataBuilder.registerRequest("John", "john@mail.com", "BUYER");

        when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(Optional.of(TestDataBuilder.user(1L, "john@mail.com", "John", true, Set.of())));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void register_shouldThrow_whenRoleDoesNotExist() {
        RegisterRequest request = TestDataBuilder.registerRequest("John", "john@mail.com", "BUYER");

        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("BUYER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role not found");
    }

    @Test
    void login_shouldAuthenticateAndSetContext() {
        LoginRequest request = TestDataBuilder.loginRequest("john@mail.com", "secret");
        Authentication authentication = new UsernamePasswordAuthenticationToken("john@mail.com", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        AuthResponse response = authService.login(request);

        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    void login_shouldThrow_whenCredentialsAreInvalid() {
        LoginRequest request = TestDataBuilder.loginRequest("john@mail.com", "wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");
    }
}
