package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.admin.AdminAnalyticsDto;
import com.notes.notesmarketplace.dto.admin.AdminNoteDto;
import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import com.notes.notesmarketplace.dto.admin.UserStatusUpdateRequest;
import com.notes.notesmarketplace.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserDto> getUsers(@PageableDefault(size = 20) Pageable pageable) {
        return adminService.getUsers(pageable);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable("id") @Positive Long id) {
        adminService.deleteUser(id);
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserDto updateUserStatus(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        return adminService.updateUserStatus(id, request.getEnabled(), authentication.getName());
    }

    @GetMapping("/notes")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminNoteDto> getNotes(@PageableDefault(size = 20) Pageable pageable) {
        return adminService.getNotes(pageable);
    }

    @DeleteMapping("/notes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteNote(@PathVariable("id") @Positive Long id) {
        adminService.deleteNote(id);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminAnalyticsDto getAnalytics() {
        return adminService.getAnalytics();
    }
}
