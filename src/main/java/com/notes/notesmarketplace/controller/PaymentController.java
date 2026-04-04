package com.notes.notesmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.notes.notesmarketplace.dto.PaymentRequest;
import com.notes.notesmarketplace.service.OrderService;
import com.notes.notesmarketplace.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/pay")
    @ResponseBody
    public ResponseEntity<?> createPayment(
            @RequestBody PaymentRequest request,
            Authentication authentication
    ) {

        String redirectUrl = paymentService.createPaymentSession(
                authentication.getName(),
                request.getNoteId()
        );

        return ResponseEntity.ok(redirectUrl);
    }

    @PostMapping("/success")
    public String paymentSuccess(
            @RequestParam String tran_id,
            @RequestParam(required = false) String status
    ) {

        // Validate status (optional, only if SSLCommerz sends it)
        if (status != null && !status.equals("VALID")) {
            return "redirect:/payment/failed?error=validation";
        }

        // Get pending payment info
        Long buyerId = paymentService.getBuyerId(tran_id);
        Long noteId = paymentService.getNoteId(tran_id);

        if (buyerId == null || noteId == null) {
            return "redirect:/payment/failed?error=invalid_transaction";
        }

        // Create order
        orderService.createOrder(buyerId, noteId, tran_id);

        // Remove from pending payments
        paymentService.removePendingPayment(tran_id);

        return "redirect:/payment/success?tran_id=" + tran_id;
    }

    @PostMapping("/fail")
    public String paymentFail(
            @RequestParam String tran_id
    ) {
        // Clean up pending payment
        paymentService.removePendingPayment(tran_id);

        return "redirect:/payment/failed?tran_id=" + tran_id;
    }

    @PostMapping("/cancel")
    public String paymentCancel(
            @RequestParam String tran_id
    ) {
        // Clean up pending payment on cancellation as well to prevent stale entries
        paymentService.removePendingPayment(tran_id);

        return "redirect:/payment/cancelled?tran_id=" + tran_id;
    }

}
