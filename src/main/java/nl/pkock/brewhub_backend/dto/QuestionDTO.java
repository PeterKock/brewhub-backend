package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

@Data
public class CreateQuestionRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 150, message = "Title must be between 10 and 150 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 2000, message = "Content must be between 20 and 2000 characters")
    private String content;
}