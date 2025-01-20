package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.VoteType;

@Data
public class VoteDTO {
    private Long id;
    private VoteType type;
    private Long userId;
}

