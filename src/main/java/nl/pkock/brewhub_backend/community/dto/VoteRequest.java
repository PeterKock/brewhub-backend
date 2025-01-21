package nl.pkock.brewhub_backend.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nl.pkock.brewhub_backend.community.models.VoteType;

@Data
public class VoteRequest {
    @NotNull(message = "Vote type is required")
    private VoteType type;

    private Long questionId;
    private Long answerId;
}
