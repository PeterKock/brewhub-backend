package nl.pkock.brewhub_backend.rating.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateRatingRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;

    @NotNull
    private Long orderId;


}
