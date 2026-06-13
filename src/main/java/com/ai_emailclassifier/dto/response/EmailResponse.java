package com.ai_emailclassifier.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;


// @Builder - Fluent construction pattern — readable and immutable-friendly.
//This is what the client sees — we control exactly what fields are exposed.

@Data
@Builder
public class EmailResponse {
    private Long id;
    //For swagger description
    @Schema(
            description = "Subject of the email",
            example = "Win an iPhone!"
    )
    private String subject;

    @Schema(
            description = "Body of the email",
            example = "Click here to claim your prize."
    )
    private String body;
    @Schema(
            description = "AI classification result",
            example = "SPAM"
    )
    private String classification;
    private Double confidenceScore;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}