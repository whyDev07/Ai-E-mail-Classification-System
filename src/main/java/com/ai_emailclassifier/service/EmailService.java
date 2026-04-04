package com.ai_emailclassifier.service;

import com.ai_emailclassifier.dto.request.EmailRequest;
import com.ai_emailclassifier.dto.response.EmailResponse;
import com.ai_emailclassifier.entity.Email;
import com.ai_emailclassifier.exception.ResourceNotFoundException;
import com.ai_emailclassifier.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final AiClassificationService aiClassificationService;

    @Transactional
    public EmailResponse classifyAndSave(EmailRequest request) {
        log.info("Starting email classification for subject: '{}'",
                request.getSubject());

        //Now further procedure in steps
        //Step 1: Calling AI
        AiClassificationService.ClassificationResult result =
                aiClassificationService.classify(request.getSubject(), request.getBody());

        // Step 2: Building entity and persist
        Email email = Email.builder()
                .subject(request.getSubject())
                .body(request.getBody())
                .classification(result.label())
                .confidenceScore(result.confidenceScore())
                .build();

        Email saved = emailRepository.save(email);
        log.info("Email saved with ID: {}, Classification: {}", saved.getId(), saved.getClassification());

        return mapToResponse(saved);
    }
    /*
     * why @Transactional(readOnly = true) on reads?
     * Tells Hibernate to skip dirty-checking — faster reads, less memory.
     */
    @Transactional(readOnly = true)
    public List<EmailResponse> getAllEmails() {
        log.debug("Fetching all emails");
        return emailRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmailResponse getEmailById(Long id) {
        log.debug("Fetching email with ID: {}", id);
        Email email = emailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email not found with ID: " + id));
        return mapToResponse(email);
    }

    //private mapper to Keep DTO mapping logic in one place.
    //If the entity or response DTO changes, only this method needs updating.
    private EmailResponse mapToResponse(Email email) {
        return EmailResponse.builder()
                .id(email.getId())
                .subject(email.getSubject())
                .body(email.getBody())
                .classification(email.getClassification())
                .confidenceScore(email.getConfidenceScore())
                .createdAt(email.getCreatedAt())
                .build();
    }
}