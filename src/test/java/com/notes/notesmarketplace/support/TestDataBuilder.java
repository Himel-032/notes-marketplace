package com.notes.notesmarketplace.support;

import com.notes.notesmarketplace.dto.LoginRequest;
import com.notes.notesmarketplace.dto.NoteUpdateRequest;
import com.notes.notesmarketplace.dto.NoteUploadRequest;
import com.notes.notesmarketplace.dto.RegisterRequest;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public final class TestDataBuilder {

    private TestDataBuilder() {
    }

    public static Role role(Long id, String roleName) {
        return new Role(id, roleName);
    }

    public static User user(Long id, String email, String name, boolean enabled, Set<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setPassword("encoded-password");
        user.setEnabled(enabled);
        user.setRoles(roles != null ? roles : new HashSet<>());
        return user;
    }

    public static Note note(Long id, String title, User seller) {
        return Note.builder()
                .id(id)
                .title(title)
                .description("desc")
                .category("Science")
                .price(100.0)
                .pdfUrl("https://cdn.example.com/" + title + ".pdf")
                .previewImageUrl("https://cdn.example.com/" + title + ".png")
                .seller(seller)
                .build();
    }

    public static Order order(Long id, User buyer, double amount, String tranId) {
        return Order.builder()
                .id(id)
                .buyer(buyer)
                .totalPrice(amount)
                .status("PAID")
                .transactionId(tranId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RegisterRequest registerRequest(String name, String email, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("Password123!");
        request.setRole(role);
        return request;
    }

    public static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    public static NoteUpdateRequest noteUpdateRequest() {
        NoteUpdateRequest request = new NoteUpdateRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated Desc");
        request.setCategory("Math");
        request.setPrice(150.0);
        return request;
    }

    public static NoteUploadRequest noteUploadRequestPdf() {
        NoteUploadRequest request = new NoteUploadRequest();
        request.setTitle("Physics Note");
        request.setDescription("chapter 1");
        request.setCategory("Physics");
        request.setPrice(120.0);
        request.setFile(new MockMultipartFile(
                "file",
                "note.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        ));
        return request;
    }
}
