package com.notes.notesmarketplace.config;

import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.RoleRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String LEGACY_ADMIN_EMAIL = "himel@gmail.com";
    private static final String ADMIN_DEFAULT_PASSWORD = "admin123";
    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        migrateAdminEmail();
        ensureAdminExists();
    }

    private void seedRoles() {
        for (String roleName : List.of("ADMIN", "SELLER", "BUYER")) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    /**
     * Migrate legacy admin email (himel@gmail.com → admin@gmail.com).
     * Only runs if the old email exists and the new one does not.
     */
    private void migrateAdminEmail() {
        Optional<User> legacyAdmin = userRepository.findByEmail(LEGACY_ADMIN_EMAIL);
        if (legacyAdmin.isPresent() && userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            User admin = legacyAdmin.get();
            admin.setEmail(ADMIN_EMAIL);
            userRepository.save(admin);
            log.info("Migrated admin email from {} to {}", LEGACY_ADMIN_EMAIL, ADMIN_EMAIL);
        }
    }

    /**
     * Create a default admin account if none exists.
     */
    private void ensureAdminExists() {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRoles().stream()
                        .anyMatch(r -> ADMIN_ROLE.equals(r.getRoleName())));

        if (!adminExists) {
            Role adminRole = roleRepository.findByRoleName(ADMIN_ROLE)
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found after seeding"));

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_DEFAULT_PASSWORD));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
            log.info("Created default admin account: {}", ADMIN_EMAIL);
        }
    }
}
