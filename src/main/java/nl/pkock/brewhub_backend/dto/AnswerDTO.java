package nl.pkock.brewhub_backend.dto;

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

@Data
public class CreateAnswerRequest {
    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 2000, message = "Content must be between 20 and 2000 characters")
    private String content;

    @NotNull(message = "Question ID is required")
    private Long questionId;
}