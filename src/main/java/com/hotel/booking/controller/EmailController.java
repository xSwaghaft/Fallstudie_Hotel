package com.hotel.booking.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.booking.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody Map<String, String> payload) {
        String to = payload.getOrDefault("to", "");
        String subject = payload.getOrDefault("subject", "Test Email");
        String body = payload.getOrDefault("body", "This is a test email from HotelBookingApp.");

        if (to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "'to' field is required"));
        }

        try {
            emailService.sendSimpleMessage(to, subject, body);
            return ResponseEntity.ok(Map.of("status", "sent", "to", to));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
