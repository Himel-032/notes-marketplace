package com.notes.notesmarketplace.controller;

import com.notes.notesmarketplace.dto.admin.AdminAnalyticsDto;
import com.notes.notesmarketplace.dto.admin.AdminNoteDto;
import com.notes.notesmarketplace.dto.admin.AdminUserDto;
import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.Order;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.OrderItemRepository;
import com.notes.notesmarketplace.repository.OrderRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AdminAnalyticsDto analytics = adminService.getAnalytics();
        List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(8)
                .toList();

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("totalUsers", analytics.getTotalUsers());
        model.addAttribute("totalNotes", analytics.getTotalNotes());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalRevenue", analytics.getTotalSales());
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("recentNotes", noteRepository.findAll().stream()
                .sorted(Comparator.comparing(Note::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .limit(6)
                .toList());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "") String role,
            @RequestParam(required = false, defaultValue = "") String status,
            Model model
    ) {
        List<AdminUserDto> users = adminService.getUsers(Pageable.unpaged()).getContent().stream()
            .sorted(Comparator.comparing(AdminUserDto::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .filter(user -> matchesUserQuery(user, q))
                .filter(user -> matchesRole(user, role))
                .filter(user -> matchesStatus(user, status))
                .toList();

        long activeUsers = users.stream().filter(AdminUserDto::isEnabled).count();

        model.addAttribute("activePage", "users");
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("disabledUsers", users.size() - activeUsers);

        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(value -> adminService.updateUserStatus(id, !value.isEnabled()));
        return "redirect:/admin/users?updated=true";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return "redirect:/admin/users?deleted=true";
    }

    @GetMapping("/notes")
    public String notes(
            @RequestParam(required = false, defaultValue = "") String q,
            Model model
    ) {
        List<AdminNoteDto> notes = adminService.getNotes(Pageable.unpaged()).getContent().stream()
            .sorted(Comparator.comparing(AdminNoteDto::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .filter(note -> matchesNoteQuery(note, q))
                .toList();

        model.addAttribute("activePage", "notes");
        model.addAttribute("pageTitle", "Notes Management");
        model.addAttribute("notes", notes);
        model.addAttribute("q", q);

        return "admin/notes";
    }

    @GetMapping("/notes/view/{id}")
    public ResponseEntity<byte[]> viewNotePdf(@PathVariable Long id) throws IOException {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        byte[] pdfBytes = URI.create(note.getPdfUrl()).toURL().openStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(note.getTitle() + ".pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id) {
        adminService.deleteNote(id);
        return "redirect:/admin/notes?deleted=true";
    }

    @GetMapping("/orders")
    public String orders(
            @RequestParam(required = false, defaultValue = "") String status,
            Model model
    ) {
        List<Order> orders = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .filter(order -> matchesOrderStatus(order, status))
                .toList();

        model.addAttribute("activePage", "orders");
        model.addAttribute("pageTitle", "Orders Management");
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);

        return "admin/orders";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        AdminAnalyticsDto analytics = adminService.getAnalytics();
        List<Order> allOrders = orderRepository.findAll();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
        Map<LocalDate, Double> salesByDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            salesByDay.put(day, 0.0);
        }

        for (Order order : allOrders) {
            if (order.getCreatedAt() == null || order.getTotalPrice() == null || !"PAID".equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            LocalDate orderDay = order.getCreatedAt().toLocalDate();
            if (salesByDay.containsKey(orderDay)) {
                salesByDay.put(orderDay, salesByDay.get(orderDay) + order.getTotalPrice());
            }
        }

        List<String> salesLabels = salesByDay.keySet().stream().map(dateFormatter::format).toList();
        List<Double> salesData = new ArrayList<>(salesByDay.values());

        List<OrderItemRepository.TopSellingNoteProjection> topSellingNotes =
            orderItemRepository.findTopSellingPaidNotes(Pageable.ofSize(5));
        List<String> topNoteLabels = topSellingNotes.stream()
            .map(OrderItemRepository.TopSellingNoteProjection::getTitle)
            .toList();
        List<Long> topNoteData = topSellingNotes.stream()
            .map(OrderItemRepository.TopSellingNoteProjection::getSoldCount)
            .toList();

        model.addAttribute("activePage", "analytics");
        model.addAttribute("pageTitle", "Analytics");
        model.addAttribute("totalUsers", analytics.getTotalUsers());
        model.addAttribute("totalNotes", analytics.getTotalNotes());
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalRevenue", analytics.getTotalSales());
        model.addAttribute("salesLabels", salesLabels);
        model.addAttribute("salesData", salesData);
        model.addAttribute("topNoteLabels", topNoteLabels);
        model.addAttribute("topNoteData", topNoteData);

        return "admin/analytics";
    }

    private boolean matchesUserQuery(AdminUserDto user, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String query = q.toLowerCase(Locale.ENGLISH);
        return safe(user.getName()).contains(query) || safe(user.getEmail()).contains(query);
    }

    private boolean matchesRole(AdminUserDto user, String role) {
        if (role == null || role.isBlank()) {
            return true;
        }
        return user.getRoles().stream().anyMatch(r -> role.equalsIgnoreCase(r));
    }

    private boolean matchesStatus(AdminUserDto user, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return user.isEnabled();
        }
        if ("DISABLED".equalsIgnoreCase(status)) {
            return !user.isEnabled();
        }
        return true;
    }

    private boolean matchesNoteQuery(AdminNoteDto note, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String query = q.toLowerCase(Locale.ENGLISH);
        return safe(note.getTitle()).contains(query)
                || safe(note.getCategory()).contains(query)
                || safe(note.getSellerEmail()).contains(query);
    }

    private boolean matchesOrderStatus(Order order, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return status.equalsIgnoreCase(order.getStatus());
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ENGLISH);
    }
}
