package com.notes.notesmarketplace.service.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.notes.notesmarketplace.model.Note;
import com.notes.notesmarketplace.model.User;
import com.notes.notesmarketplace.repository.NoteRepository;
import com.notes.notesmarketplace.repository.UserRepository;
import com.notes.notesmarketplace.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    // Store pending payments temporarily: transactionId -> {buyerId, noteId}
    private final Map<String, Map<String, Long>> pendingPayments = new ConcurrentHashMap<>();

    @Value("${sslcommerz.store-id}")
    private String storeId;

    @Value("${sslcommerz.store-password}")
    private String storePassword;

    @Value("${sslcommerz.base-url}")
    private String sslUrl;

    @Override
    public String createPaymentSession(String email, Long noteId) {

        try {

            User buyer = userRepository.findByEmail(email).orElseThrow();
            Note note = noteRepository.findById(noteId).orElseThrow();

            String tranId = UUID.randomUUID().toString();

            // Store pending payment info
            storePendingPayment(tranId, buyer.getId(), noteId);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            params.add("store_id", storeId);
            params.add("store_passwd", storePassword);
            params.add("total_amount", note.getPrice().toString());
            params.add("currency", "BDT");
            params.add("tran_id", tranId);

            params.add("success_url", "https://notes-marketplace.onrender.com/api/payment/success");
            params.add("fail_url", "https://notes-marketplace.onrender.com/api/payment/fail");
            params.add("cancel_url", "https://notes-marketplace.onrender.com/api/payment/cancel");

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
    public void storePendingPayment(String transactionId, Long buyerId, Long noteId) {
        Map<String, Long> paymentInfo = new ConcurrentHashMap<>();
        paymentInfo.put("buyerId", buyerId);
        paymentInfo.put("noteId", noteId);
        pendingPayments.put(transactionId, paymentInfo);
    }

    @Override
    public Long getBuyerId(String transactionId) {
        Map<String, Long> paymentInfo = pendingPayments.get(transactionId);
        return paymentInfo != null ? paymentInfo.get("buyerId") : null;
    }

    @Override
    public Long getNoteId(String transactionId) {
        Map<String, Long> paymentInfo = pendingPayments.get(transactionId);
        return paymentInfo != null ? paymentInfo.get("noteId") : null;
    }

    @Override
    public void removePendingPayment(String transactionId) {
        pendingPayments.remove(transactionId);
    }
}
