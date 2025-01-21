package nl.pkock.brewhub_backend.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerDTO {
    private Long id;
    private String content;
    private String authorName;
    private Long authorId;
    private boolean isRetailerResponse;
    private boolean isAccepted;
    private boolean isVerified;
    private int voteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private VoteDTO userVote;
}

