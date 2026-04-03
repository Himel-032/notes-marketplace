package com.notes.notesmarketplace.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.notes.notesmarketplace.model.Role;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.RoleRepository;
import com.notes.notesmarketplace.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {
        try {
            // Prevent ADMIN registration through form
            if ("ADMIN".equalsIgnoreCase(role)) {
                model.addAttribute("error", "Admin registration is not allowed!");
                return "auth/register";
            }

            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                model.addAttribute("error", "Email already registered!");
                return "auth/register";
            }

            // Validate role is only BUYER or SELLER
            if (!"BUYER".equals(role) && !"SELLER".equals(role)) {
                model.addAttribute("error", "Invalid role selected!");
                return "auth/register";
            }

            // Find role
            Role userRole = roleRepository.findByRoleName(role)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + role));

            // Create new user
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.getRoles().add(userRole);

            userRepository.save(user);

            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/seller/dashboard")
    public String sellerDashboard() {
        return "seller/seller-dashboard";
    }

    @GetMapping("/seller/notes")
    public String sellerNotes() {
        return "seller/seller-notes";
    }

    @GetMapping("/seller/upload-note")
    public String sellerUploadNote() {
        return "seller/seller-upload-note";
    }

    @GetMapping("/buyer/dashboard")
    public String buyerDashboard() {
        return "buyer/buyer-dashboard";
    }

    @GetMapping("/buyer/browse")
    public String buyerBrowse() {
        return "buyer/buyer-browse";
    }

    @GetMapping("/buyer/my-downloads")
    public String buyerDownloads() {
        return "buyer/buyer-downloads";
    }

    @GetMapping("/buyer/orders")
    public String buyerOrders() {
        return "buyer/buyer-orders";
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (name != null && !name.isBlank()) {
                user.setName(name.trim());
            }
            user.setPhone(phone != null ? phone.trim() : null);

            if (password != null && !password.isBlank()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess() {
        return "payment/payment-success";
    }

    @GetMapping({"/payment/failed", "/payment/fail"})
    public String paymentFailedView() {
        return "payment/payment-failed";
    }

    @GetMapping({"/payment/cancelled", "/payment/cancel"})
    public String paymentCancelledView() {
        return "payment/payment-cancelled";
    }
}
