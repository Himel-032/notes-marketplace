package com.notes.notesmarketplace.service;

import com.notes.notesmarketplace.dto.admin.AdminAnalyticsDto;
import com.notes.notesmarketplace.dto.admin.AdminNoteDto;
import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<AdminUserDto> getUsers(Pageable pageable);

    void deleteUser(Long userId);

    AdminUserDto updateUserStatus(Long userId, boolean enabled, String currentAdminEmail);

    Page<AdminNoteDto> getNotes(Pageable pageable);

    void deleteNote(Long noteId);

    AdminAnalyticsDto getAnalytics();
}
