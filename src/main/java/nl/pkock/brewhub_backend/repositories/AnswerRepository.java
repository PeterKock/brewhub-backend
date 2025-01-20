package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionIdAndIsActiveTrue(Long questionId);

    List<Answer> findByAuthorIdAndIsActiveTrue(Long authorId);

    long countByQuestionIdAndIsActiveTrue(Long questionId);
}