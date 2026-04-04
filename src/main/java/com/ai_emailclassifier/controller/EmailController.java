package com.ai_emailclassifier.controller;

import com.ai_emailclassifier.dto.request.EmailRequest;
import com.ai_emailclassifier.dto.response.EmailResponse;
import com.ai_emailclassifier.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/classify-email")
    public ResponseEntity<EmailResponse> classifyEmail(
            @Valid @RequestBody EmailRequest request) {
        log.info("Received classification request for subject: '{}'", request.getSubject());
        EmailResponse response = emailService.classifyAndSave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //  @PreAuthorize - All emails is sensitive — admins should see them only

    @GetMapping("/emails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailResponse>> getAllEmails() {
        log.info("Admin fetching all emails");
        return ResponseEntity.ok(emailService.getAllEmails());
    }

    @GetMapping("/email/{id}")
    public ResponseEntity<EmailResponse> getEmailById(@PathVariable Long id) {
        log.info("Fetching email by ID: {}", id);
        return ResponseEntity.ok(emailService.getEmailById(id));
    }
}