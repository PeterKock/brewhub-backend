package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Vote;
import nl.pkock.brewhub_backend.models.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndQuestionId(Long userId, Long questionId);
    Optional<Vote> findByUserIdAndAnswerId(Long userId, Long answerId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.question.id = :questionId AND v.type = :type")
    long countByQuestionIdAndType(@Param("questionId") Long questionId, @Param("type") VoteType type);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.answer.id = :answerId AND v.type = :type")
    long countByAnswerIdAndType(@Param("answerId") Long answerId, @Param("type") VoteType type);
}