package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    // Store pending payments temporarily: transactionId -> payment info
    private final Map<String, PendingPaymentInfo> pendingPayments = new ConcurrentHashMap<>();

    @Value("${sslcommerz.store-id}")
    private String storeId;

    @Value("${sslcommerz.store-password}")
    private String storePassword;

    @Value("${sslcommerz.base-url}")
    private String sslUrl;

    @Override
    public String createPaymentSession(String email, Long noteId, String paymentMethod) {

        try {

            User buyer = userRepository.findByEmail(email).orElseThrow();
            Note note = noteRepository.findById(noteId).orElseThrow();

            String tranId = UUID.randomUUID().toString();

            // Store pending payment info
            storePendingPayment(tranId, buyer.getId(), noteId, paymentMethod);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            params.add("store_id", storeId);
            params.add("store_passwd", storePassword);
            params.add("total_amount", note.getPrice().toString());
            params.add("currency", "BDT");
            params.add("tran_id", tranId);

            params.add("success_url", "http://localhost:8080/api/payment/success");
            params.add("fail_url", "http://localhost:8080/api/payment/fail");
            params.add("cancel_url", "http://localhost:8080/api/payment/cancel");

            params.add("product_name", note.getTitle());
            params.add("product_category", note.getCategory());

            params.add("cus_name", buyer.getName());
            params.add("cus_email", buyer.getEmail());

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    sslUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
            }
            );

            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("GatewayPageURL")) {
                throw new RuntimeException("Invalid response from payment gateway");
            }

            return body.get("GatewayPageURL").toString();

        } catch (Exception e) {
            throw new RuntimeException("Payment initialization failed: " + e.getMessage());
        }
    }

    @Override
    public void storePendingPayment(String transactionId, Long buyerId, Long noteId, String paymentMethod) {
        PendingPaymentInfo paymentInfo = new PendingPaymentInfo(
                buyerId,
                noteId,
                normalizePaymentMethod(paymentMethod)
        );
        pendingPayments.put(transactionId, paymentInfo);
    }

    @Override
    public Long getBuyerId(String transactionId) {
        PendingPaymentInfo paymentInfo = pendingPayments.get(transactionId);
        return paymentInfo != null ? paymentInfo.buyerId() : null;
    }

    @Override
    public Long getNoteId(String transactionId) {
        PendingPaymentInfo paymentInfo = pendingPayments.get(transactionId);
        return paymentInfo != null ? paymentInfo.noteId() : null;
    }

    @Override
    public String getPaymentMethod(String transactionId) {
        PendingPaymentInfo paymentInfo = pendingPayments.get(transactionId);
        return paymentInfo != null ? paymentInfo.paymentMethod() : null;
    }

    @Override
    public void removePendingPayment(String transactionId) {
        pendingPayments.remove(transactionId);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "CARD";
        }
        return paymentMethod.toUpperCase(Locale.ROOT);
    }

    private record PendingPaymentInfo(Long buyerId, Long noteId, String paymentMethod) {
    }
}
