package nl.pkock.brewhub_backend.community.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.dto.VoteRequest;
import nl.pkock.brewhub_backend.community.models.Answer;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.Vote;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.community.repositories.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void voteOnQuestion(VoteRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Vote existingVote = voteRepository.findByUserIdAndQuestionId(userId, question.getId())
                .orElse(null);

        if (existingVote != null) {
            if (existingVote.getType() == request.getType()) {
                // Remove vote if clicking the same type again
                voteRepository.delete(existingVote);
            } else {
                // Change vote type if voting differently
                existingVote.setType(request.getType());
                voteRepository.save(existingVote);
            }
        } else {
            // Create new vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setQuestion(question);
            vote.setType(request.getType());
            voteRepository.save(vote);
        }
    }

    @Transactional
    public void voteOnAnswer(VoteRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Vote existingVote = voteRepository.findByUserIdAndAnswerId(userId, answer.getId())
                .orElse(null);

        if (existingVote != null) {
            if (existingVote.getType() == request.getType()) {
                // Remove vote if clicking the same type again
                voteRepository.delete(existingVote);
            } else {
                // Change vote type if voting differently
                existingVote.setType(request.getType());
                voteRepository.save(existingVote);
            }
        } else {
            // Create new vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setAnswer(answer);
            vote.setType(request.getType());
            voteRepository.save(vote);
        }
    }
}