package com.ai_emailclassifier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String subject;

    //columnDefinition TEXT? -Email bodies can be thousands of characters
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 50)
    private String classification;  //SPAM,IMPORTANT,PROMOTIONS,SOCIAL

    //WHY Double nullable - AI might not always return confidence score
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}