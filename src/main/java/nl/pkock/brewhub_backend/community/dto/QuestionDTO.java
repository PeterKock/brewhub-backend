package nl.pkock.brewhub_backend.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long authorId;
    private boolean isRetailerResponse;
    private int voteCount;
    private int answerCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPinned;
    private VoteDTO userVote;
}

