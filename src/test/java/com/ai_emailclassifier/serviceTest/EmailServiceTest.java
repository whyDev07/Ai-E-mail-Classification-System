package com.ai_emailclassifier.serviceTest;

import com.ai_emailclassifier.dto.request.EmailRequest;
import com.ai_emailclassifier.dto.response.EmailResponse;
import com.ai_emailclassifier.entity.Email;
import com.ai_emailclassifier.entity.EmailCategory;
import com.ai_emailclassifier.exception.AiServiceException;
import com.ai_emailclassifier.exception.ResourceNotFoundException;
import com.ai_emailclassifier.repository.EmailRepository;
import com.ai_emailclassifier.service.AiClassificationService;
import com.ai_emailclassifier.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private EmailRepository emailRepository;

    @Mock
    private AiClassificationService aiClassificationService;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldClassifyAndSaveEmail(){
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Congratulations you Won");
        emailRequest.setBody("You've actually won the Bonanza prize for having the weirdest fart!");

        AiClassificationService.ClassificationResult aiResult = new
                AiClassificationService.ClassificationResult(EmailCategory.SPAM,0.98);

        Email savedEmail = Email.builder()
                .id(1L)
                .subject(emailRequest.getSubject())
                .body(emailRequest.getBody())
                .classification(aiResult.label())
                .confidenceScore(aiResult.confidenceScore())
                .createdAt(LocalDateTime.now())
                .build();

        when(aiClassificationService.classify
                (emailRequest.getSubject(),emailRequest.getBody()))
                .thenReturn(aiResult);

        when(emailRepository.save(any(Email.class)))
                .thenReturn(savedEmail);


        EmailResponse emailResponse = emailService.classifyAndSave(emailRequest);

        assertNotNull(emailResponse);
        assertEquals(EmailCategory.SPAM, emailResponse.getClassification());
        assertEquals(emailRequest.getSubject(),emailResponse.getSubject());
        assertEquals(0.98, emailResponse.getConfidenceScore());

        verify(aiClassificationService)
                .classify(emailResponse.getSubject(),emailResponse.getBody());

        verify(emailRepository)
                .save(any(Email.class));
    }

    @Test
    void shouldThrowExceptionWhenAiFails() {

        EmailRequest request = new EmailRequest();
        request.setSubject("Win an iPhone");
        request.setBody("Click here to claim.");

        when(aiClassificationService.classify(
                request.getSubject(),
                request.getBody()))
                .thenThrow(new AiServiceException("AI service unavailable"));

        assertThrows(
                AiServiceException.class,
                () -> emailService.classifyAndSave(request)
        );

        verify(aiClassificationService)
                .classify(request.getSubject(), request.getBody());

        verify(emailRepository, never())
                .save(any(Email.class));
    }

    @Test
    void shouldReturnEmailById() {

        Long emailId = 1L;

        Email email = Email.builder()
                .id(emailId)
                .subject("Meeting Reminder")
                .body("Don't forget the meeting at 10 AM.")
                .classification(EmailCategory.IMPORTANT)
                .confidenceScore(0.98)
                .createdAt(LocalDateTime.now())
                .build();

        when(emailRepository.findById(emailId))
                .thenReturn(Optional.of(email));

        EmailResponse response = emailService.getEmailById(emailId);

        assertNotNull(response);
        assertEquals(emailId, response.getId());
        assertEquals("Meeting Reminder", response.getSubject());
        assertEquals(EmailCategory.IMPORTANT, response.getClassification());

        verify(emailRepository).findById(emailId);
    }

    @Test
    void shouldThrowWhenEmailNotFound() {

        Long emailId = 999L;

        when(emailRepository.findById(emailId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> emailService.getEmailById(emailId)
        );

        assertEquals(
                "Email not found with ID: " + emailId,
                exception.getMessage()
        );

        verify(emailRepository).findById(emailId);
    }

    @Test
    void shouldReturnAllEmails() {

        Email email1 = Email.builder()
                .id(1L)
                .subject("Meeting")
                .body("Meeting at 10 AM")
                .classification(EmailCategory.IMPORTANT)
                .confidenceScore(0.95)
                .createdAt(LocalDateTime.now())
                .build();

        Email email2 = Email.builder()
                .id(2L)
                .subject("Amazon Sale")
                .body("Up to 70% off")
                .classification(EmailCategory.PROMOTIONS)
                .confidenceScore(0.90)
                .createdAt(LocalDateTime.now())
                .build();

        when(emailRepository.findAllOrderByCreatedAtDesc())
                .thenReturn(List.of(email1, email2));

        List<EmailResponse> responses = emailService.getAllEmails();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("Meeting", responses.get(0).getSubject());
        assertEquals(EmailCategory.IMPORTANT, responses.get(0).getClassification());

        assertEquals("Amazon Sale", responses.get(1).getSubject());
        assertEquals(EmailCategory.PROMOTIONS, responses.get(1).getClassification());

        verify(emailRepository).findAllOrderByCreatedAtDesc();
    }
}
