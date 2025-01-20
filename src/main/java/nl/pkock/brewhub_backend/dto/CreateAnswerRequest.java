package nl.pkock.brewhub_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAnswerRequest {
    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 2000, message = "Content must be between 20 and 2000 characters")
    private String content;

    @NotNull(message = "Question ID is required")
    private Long questionId;
}
