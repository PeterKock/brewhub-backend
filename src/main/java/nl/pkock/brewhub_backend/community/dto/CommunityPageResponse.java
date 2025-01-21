package nl.pkock.brewhub_backend.community.dto;

import lombok.Data;
import java.util.List;

@Data
public class CommunityPageResponse {
    private List<QuestionDTO> pinnedQuestions;
    private List<QuestionDTO> recentQuestions;
    private List<QuestionDTO> popularQuestions;
    private int totalQuestions;
    private int totalAnswers;
    private List<String> popularTags;
}