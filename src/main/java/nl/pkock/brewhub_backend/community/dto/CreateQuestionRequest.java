package nl.pkock.brewhub_backend.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateQuestionRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 150, message = "Title must be between 10 and 150 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 2000, message = "Content must be between 20 and 2000 characters")
    private String content;
}
