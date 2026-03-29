package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.dto.admin.AdminNoteDto;
import com.notes.notesmarketplace.dto.admin.AdminAnalyticsDto;
import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import com.notes.notesmarketplace.exception.ResourceNotFoundException;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.CloudinaryService;
import com.notes.notesmarketplace.support.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getAllUsers() {
        User admin = TestDataBuilder.user(1L, "admin@mail.com", "Admin", true, Set.of(new Role(1L, "ADMIN")));
        Page<User> userPage = new PageImpl<>(List.of(admin));
        when(userRepository.findAll(Pageable.unpaged())).thenReturn(userPage);

        Page<AdminUserDto> result = adminService.getUsers(Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("admin@mail.com");
    }

    @Test
    void deleteUser() {
        User buyer = TestDataBuilder.user(5L, "buyer@mail.com", "Buyer", true, Set.of(new Role(2L, "BUYER")));
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));
        when(noteRepository.findBySeller(buyer)).thenReturn(List.of());
        when(orderRepository.findByBuyerId(5L)).thenReturn(List.of());

        adminService.deleteUser(5L);

        verify(userRepository).delete(buyer);
    }

    @Test
    void getAllNotes() {
        User seller = TestDataBuilder.user(2L, "seller@mail.com", "Seller", true, Set.of(new Role(3L, "SELLER")));
        Note note = TestDataBuilder.note(11L, "Core Java", seller);
        Page<Note> notePage = new PageImpl<>(List.of(note));
        when(noteRepository.findAll(Pageable.unpaged())).thenReturn(notePage);

        Page<AdminNoteDto> result = adminService.getNotes(Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Core Java");
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: 404");
    }

    @Test
    void updateUserStatus() {
        User buyer = TestDataBuilder.user(9L, "buyer@mail.com", "Buyer", false, Set.of(new Role(2L, "BUYER")));

        when(userRepository.findById(9L)).thenReturn(Optional.of(buyer));
        when(userRepository.save(buyer)).thenReturn(buyer);

        AdminUserDto updated = adminService.updateUserStatus(9L, true);

        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getId()).isEqualTo(9L);
        verify(userRepository).save(buyer);
    }

    @Test
    void getAnalytics() {
        when(userRepository.count()).thenReturn(15L);
        when(noteRepository.count()).thenReturn(40L);
        when(orderRepository.getTotalSales()).thenReturn(4500.75);

        AdminAnalyticsDto analytics = adminService.getAnalytics();

        assertThat(analytics.getTotalUsers()).isEqualTo(15L);
        assertThat(analytics.getTotalNotes()).isEqualTo(40L);
        assertThat(analytics.getTotalSales()).isEqualTo(4500.75);
    }
}
