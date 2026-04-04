package com.ai_emailclassifier.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;


// @Builder - Fluent construction pattern — readable and immutable-friendly.
//This is what the client sees — we control exactly what fields are exposed.

@Data
@Builder
public class EmailResponse {
    private Long id;
    private String subject;
    private String body;
    private String classification;
    private Double confidenceScore;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}