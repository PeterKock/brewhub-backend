package nl.pkock.brewhub_backend.community.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.community.models.VoteType;

@Data
public class VoteDTO {
    private Long id;
    private VoteType type;
    private Long userId;
}

