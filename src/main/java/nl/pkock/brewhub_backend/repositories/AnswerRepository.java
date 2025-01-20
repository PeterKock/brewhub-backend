package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId AND a.active = true")
    List<Answer> findByQuestionIdAndActiveTrue(@Param("questionId") Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.author.id = :authorId AND a.active = true")
    List<Answer> findByAuthorIdAndActiveTrue(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.active = true")
    long countByActiveTrue();

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.id = :questionId AND a.active = true")
    long countByQuestionIdAndActiveTrue(@Param("questionId") Long questionId);
}