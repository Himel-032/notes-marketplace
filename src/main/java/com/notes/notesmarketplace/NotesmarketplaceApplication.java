package com.notes.notesmarketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.notes.notesmarketplace.repository")
public class NotesmarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotesmarketplaceApplication.class, args);
    }
}
