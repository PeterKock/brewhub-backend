package nl.pkock.brewhub_backend.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.CommunityPageResponse;
import nl.pkock.brewhub_backend.dto.QuestionDTO;
import nl.pkock.brewhub_backend.models.Question;
import nl.pkock.brewhub_backend.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.repositories.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;

    @Transactional(readOnly = true)
    public CommunityPageResponse getCommunityPageData(Long currentUserId) {
        CommunityPageResponse response = new CommunityPageResponse();

        // Get pinned questions
        List<Question> pinnedQuestions = questionRepository.findByIsActiveTrueAndIsPinnedTrue();
        response.setPinnedQuestions(pinnedQuestions.stream()
                .map(q -> questionService.mapToDTO(q, currentUserId))
                .collect(Collectors.toList()));

        // Get recent questions
        List<Question> recentQuestions = questionRepository.findTop10ByIsActiveTrueOrderByCreatedAtDesc();
        response.setRecentQuestions(recentQuestions.stream()
                .map(q -> questionService.mapToDTO(q, currentUserId))
                .collect(Collectors.toList()));

        // Get most upvoted questions
        List<Question> popularQuestions = questionRepository.findMostUpvotedQuestions();
        response.setPopularQuestions(popularQuestions.stream()
                .map(q -> questionService.mapToDTO(q, currentUserId))
                .collect(Collectors.toList()));

        // Get statistics
        response.setTotalQuestions((int) questionRepository.countByIsActiveTrue());
        response.setTotalAnswers((int) answerRepository.countByIsActiveTrue());

        return response;
    }
}