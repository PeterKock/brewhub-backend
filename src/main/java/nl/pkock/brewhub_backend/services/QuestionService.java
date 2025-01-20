package nl.pkock.brewhub_backend.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.CreateQuestionRequest;
import nl.pkock.brewhub_backend.dto.QuestionDTO;
import nl.pkock.brewhub_backend.models.Question;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.models.Vote;
import nl.pkock.brewhub_backend.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.repositories.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Transactional(readOnly = true)
    public List<QuestionDTO> getAllQuestions(Long currentUserId) {
        return questionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(question -> mapToDTO(question, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long questionId, Long currentUserId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.isActive()) {
            throw new RuntimeException("Question is not active");
        }

        return mapToDTO(question, currentUserId);
    }

    @Transactional
    public QuestionDTO createQuestion(CreateQuestionRequest request, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setContent(request.getContent());
        question.setAuthor(author);
        question.setActive(true);
        question.setPinned(false);

        Question savedQuestion = questionRepository.save(question);
        return mapToDTO(savedQuestion, userId);
    }

    @Transactional
    public QuestionDTO updateQuestion(Long questionId, CreateQuestionRequest request, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this question");
        }

        question.setTitle(request.getTitle());
        question.setContent(request.getContent());

        Question updatedQuestion = questionRepository.save(question);
        return mapToDTO(updatedQuestion, userId);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this question");
        }

        question.setActive(false);
        questionRepository.save(question);
    }

    @Transactional
    public QuestionDTO togglePin(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Check if user has moderator/admin privileges here

        question.setPinned(!question.isPinned());
        Question updatedQuestion = questionRepository.save(question);
        return mapToDTO(updatedQuestion, userId);
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> searchQuestions(String searchTerm, Long currentUserId) {
        return questionRepository.search(searchTerm)
                .stream()
                .map(question -> mapToDTO(question, currentUserId))
                .collect(Collectors.toList());
    }

    private QuestionDTO mapToDTO(Question question, Long currentUserId) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setAuthorName(question.getAuthor().getFirstName() + " " + question.getAuthor().getLastName());
        dto.setAuthorId(question.getAuthor().getId());
        dto.setRetailerResponse(question.getAuthor().getRoles().contains(UserRole.RETAILER));
        dto.setVoteCount(calculateVoteCount(question.getVotes()));
        dto.setAnswerCount(question.getAnswers().size());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        dto.setPinned(question.isPinned());

        // Get current user's vote if exists
        if (currentUserId != null) {
            Vote userVote = voteRepository.findByUserIdAndQuestionId(currentUserId, question.getId())
                    .orElse(null);
            if (userVote != null) {
                VoteDTO voteDTO = new VoteDTO();
                voteDTO.setId(userVote.getId());
                voteDTO.setType(userVote.getType());
                voteDTO.setUserId(userVote.getUser().getId());
                dto.setUserVote(voteDTO);
            }
        }

        return dto;
    }

    private int calculateVoteCount(List<Vote> votes) {
        return (int) (votes.stream().filter(v -> v.getType() == VoteType.UPVOTE).count() -
                votes.stream().filter(v -> v.getType() == VoteType.DOWNVOTE).count());
    }
}