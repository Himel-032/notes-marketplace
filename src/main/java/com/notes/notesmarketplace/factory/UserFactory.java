package com.notes.notesmarketplace.factory;

import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class UserFactory {

    public User createUser(String name, String email, String encodedPassword, Role role) {
        String roleName = role.getRoleName().toUpperCase(Locale.ROOT);

        if (!isSupportedRole(roleName)) {
            throw new IllegalArgumentException("Unsupported role: " + role.getRoleName());
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    private boolean isSupportedRole(String roleName) {
        return "ADMIN".equals(roleName)
                || "SELLER".equals(roleName)
                || "BUYER".equals(roleName);
    }
}