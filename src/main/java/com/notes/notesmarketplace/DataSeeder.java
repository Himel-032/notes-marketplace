package com.notes.notesmarketplace;

import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        List<String> roles = List.of("ADMIN", "SELLER", "BUYER");
        for (String roleName : roles) {
            roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
        }
        System.out.println("Roles seeded successfully!");
    }
}
