package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.VoteType;

@Data
public class VoteDTO {
    private Long id;
    private VoteType type;
    private Long userId;
}

@Data
public class VoteRequest {
    @NotNull(message = "Vote type is required")
    private VoteType type;

    private Long questionId;
    private Long answerId;
}