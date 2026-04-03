package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.dto.admin.AdminAnalyticsDto;
import com.notes.notesmarketplace.dto.admin.AdminNoteDto;
import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import com.notes.notesmarketplace.exception.BusinessValidationException;
import com.notes.notesmarketplace.exception.ResourceNotFoundException;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.AdminService;
import com.notes.notesmarketplace.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!noteRepository.findBySeller(user).isEmpty()) {
            throw new BusinessValidationException("Cannot delete user with published notes");
        }

        if (!orderRepository.findByBuyerId(userId).isEmpty()) {
            throw new BusinessValidationException("Cannot delete user with existing orders");
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public AdminUserDto updateUserStatus(Long userId, boolean enabled, String currentAdminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!enabled && user.getEmail().equals(currentAdminEmail)) {
            throw new BusinessValidationException("Admin cannot disable their own account");
        }

        user.setEnabled(enabled);
        return toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminNoteDto> getNotes(Pageable pageable) {
        return noteRepository.findAll(pageable).map(this::toNoteDto);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        if (orderItemRepository.existsByNoteId(noteId)) {
            throw new BusinessValidationException("Cannot delete note linked to existing orders");
        }

        if (note.getPdfUrl() != null && !note.getPdfUrl().isBlank()) {
            cloudinaryService.deletePdf(note.getPdfUrl());
        }

        noteRepository.delete(note);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAnalyticsDto getAnalytics() {
        long totalUsers = userRepository.count();
        long totalNotes = noteRepository.count();
        double totalSales = orderRepository.getTotalSales();

        return AdminAnalyticsDto.builder()
                .totalUsers(totalUsers)
                .totalNotes(totalNotes)
                .totalSales(totalSales)
                .build();
    }

    private AdminUserDto toUserDto(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return AdminUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(roles)
                .build();
    }

    private AdminNoteDto toNoteDto(Note note) {
        return AdminNoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .category(note.getCategory())
                .price(note.getPrice())
                .sellerId(note.getSeller() != null ? note.getSeller().getId() : null)
                .sellerEmail(note.getSeller() != null ? note.getSeller().getEmail() : null)
                .pdfUrl(note.getPdfUrl())
                .build();
    }
}
