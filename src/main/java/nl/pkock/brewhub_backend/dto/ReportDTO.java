package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.ReportStatus;

@Data
public class ReportDTO {
    private Long id;
    private String reason;
    private String description;
    private ReportStatus status;
    private Long reporterId;
    private String reporterName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

@Data
public class CreateReportRequest {
    @NotBlank(message = "Reason is required")
    private String reason;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Long questionId;
    private Long answerId;
}