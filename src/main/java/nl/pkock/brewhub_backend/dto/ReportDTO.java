package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.ReportStatus;

import java.time.LocalDateTime;

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

