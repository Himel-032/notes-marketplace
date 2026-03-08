package com.notes.notesmarketplace.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.RoleRepository;
import com.notes.notesmarketplace.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        String[] roles = {"ADMIN", "SELLER", "BUYER"};

        for (String roleName : roles) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
                log.info("Role created: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }

        log.info("Roles initialization completed!");
    }

    private void initializeAdminUser() {
        String adminEmail = "admin@gmail.com";
        
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            
            userRepository.save(admin);
            log.info("Default admin user created: {}", adminEmail);
        } else {
            log.info("Admin user already exists: {}", adminEmail);
        }
        
        log.info("Data initialization completed!");
    }
}
