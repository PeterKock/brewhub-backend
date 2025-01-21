package nl.pkock.brewhub_backend.community.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.community.dto.AnswerDTO;
import nl.pkock.brewhub_backend.community.dto.CreateAnswerRequest;
import nl.pkock.brewhub_backend.community.dto.VoteDTO;
import nl.pkock.brewhub_backend.community.models.Answer;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.Vote;
import nl.pkock.brewhub_backend.community.models.VoteType;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.repositories.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Transactional(readOnly = true)
    public List<AnswerDTO> getAnswersByQuestionId(Long questionId, Long currentUserId) {
        return answerRepository.findByQuestionIdAndActiveTrue(questionId)  // Changed from IsActiveTrue
                .stream()
                .map(answer -> mapToDTO(answer, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public AnswerDTO createAnswer(CreateAnswerRequest request, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Answer answer = new Answer();
        answer.setContent(request.getContent());
        answer.setAuthor(author);
        answer.setQuestion(question);
        answer.setActive(true);
        answer.setVerifiedAnswer(author.getRoles().contains(UserRole.RETAILER));

        Answer savedAnswer = answerRepository.save(answer);
        return mapToDTO(savedAnswer, userId);
    }

    @Transactional
    public AnswerDTO updateAnswer(Long answerId, CreateAnswerRequest request, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!answer.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this answer");
        }

        answer.setContent(request.getContent());
        Answer updatedAnswer = answerRepository.save(answer);
        return mapToDTO(updatedAnswer, userId);
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!answer.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this answer");
        }

        answer.setActive(false);
        answerRepository.save(answer);
    }

    @Transactional
    public AnswerDTO acceptAnswer(Long answerId, Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only question author can accept answers");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        // Remove accepted status from any previously accepted answer
        answerRepository.findByQuestionIdAndActiveTrue(questionId)
                .stream()
                .filter(Answer::isAccepted)
                .forEach(a -> {
                    a.setAccepted(false);
                    answerRepository.save(a);
                });

        answer.setAccepted(true);
        Answer updatedAnswer = answerRepository.save(answer);
        return mapToDTO(updatedAnswer, userId);
    }

    @Transactional
    public AnswerDTO toggleVerifiedStatus(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        // Check if user has moderator privileges here

        answer.setVerifiedAnswer(!answer.isVerifiedAnswer());
        Answer updatedAnswer = answerRepository.save(answer);
        return mapToDTO(updatedAnswer, userId);
    }

    private AnswerDTO mapToDTO(Answer answer, Long currentUserId) {
        AnswerDTO dto = new AnswerDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setAuthorName(answer.getAuthor().getFirstName() + " " + answer.getAuthor().getLastName());
        dto.setAuthorId(answer.getAuthor().getId());
        dto.setRetailerResponse(answer.getAuthor().getRoles().contains(UserRole.RETAILER));
        dto.setAccepted(answer.isAccepted());
        dto.setVerified(answer.isVerifiedAnswer());
        dto.setVoteCount(calculateVoteCount(answer.getVotes()));
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setUpdatedAt(answer.getUpdatedAt());

        // Get current user's vote if exists
        if (currentUserId != null) {
            Vote userVote = voteRepository.findByUserIdAndAnswerId(currentUserId, answer.getId())
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