package nl.pkock.brewhub_backend.community.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotBlank(message = "Reason is required")
    private String reason;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Long questionId;
    private Long answerId;

    @AssertTrue(message = "Either questionId or answerId must be provided")
    private boolean isValidReport() {
        return (questionId != null) ^ (answerId != null);
    }
}
