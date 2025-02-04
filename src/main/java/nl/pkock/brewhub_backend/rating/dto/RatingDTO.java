package nl.pkock.brewhub_backend.rating.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Long id;
    private String customerName;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
    private Long orderId;
}