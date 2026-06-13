package com.ai_emailclassifier.controller;

import com.ai_emailclassifier.dto.request.EmailRequest;
import com.ai_emailclassifier.dto.response.EmailResponse;
import com.ai_emailclassifier.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Tag(
            name = "Email Classification",
            description = "Endpoints for classifying and managing emails"
    )
    @Operation(
            summary = "Classify an email",
            description = "Uses the AI model to classify an email into SPAM, IMPORTANT, SOCIAL or PROMOTIONS."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email classified successfully"),
            @ApiResponse(
                    responseCode = "503",
                    description = "AI service unavailable")
    })
    @PostMapping("/classify-email")
    public ResponseEntity<EmailResponse> classifyEmail(
            @Valid @RequestBody EmailRequest request) {
        log.info("Received classification request for subject: '{}'", request.getSubject());
        EmailResponse response = emailService.classifyAndSave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //  @PreAuthorize - All emails is sensitive — admins should see them only

    @Operation(
            summary = "Get all classified emails",
            description = "Returns all classified emails. Accessible only to ADMIN users."
    )
    @GetMapping("/emails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailResponse>> getAllEmails() {
        log.info("Admin fetching all emails");
        return ResponseEntity.ok(emailService.getAllEmails());
    }

    @Operation(
            summary = "Get email by ID",
            description = "Fetches a previously classified email using its database ID."
    )
    @GetMapping("/email/{id}")
    public ResponseEntity<EmailResponse> getEmailById(
            @Parameter(
                    description = "Unique database ID of the classified email",
                    example = "1")
            @PathVariable Long id)
    {
        log.info("Fetching email by ID: {}", id);
        return ResponseEntity.ok(emailService.getEmailById(id));
    }
}