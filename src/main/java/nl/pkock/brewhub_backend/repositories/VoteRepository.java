package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Vote;
import nl.pkock.brewhub_backend.models.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndQuestionId(Long userId, Long questionId);

    Optional<Vote> findByUserIdAndAnswerId(Long userId, Long answerId);

    long countByQuestionIdAndType(Long questionId, VoteType type);

    long countByAnswerIdAndType(Long answerId, VoteType type);
}