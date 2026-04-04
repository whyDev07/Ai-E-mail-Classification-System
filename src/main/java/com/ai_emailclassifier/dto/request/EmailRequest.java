package com.ai_emailclassifier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailRequest {

    @NotBlank(message = "Subject must not be blank")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;

    @NotBlank(message = "Body must not be blank")
    @Size(max = 10000, message = "Body must not exceed 10,000 characters")
    private String body;
}