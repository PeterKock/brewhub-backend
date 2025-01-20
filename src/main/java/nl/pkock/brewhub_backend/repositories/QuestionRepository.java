package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByIsActiveTrue();

    List<Question> findByIsActiveTrueOrderByCreatedAtDesc();

    List<Question> findByAuthorIdAndIsActiveTrue(Long authorId);

    @Query("SELECT q FROM Question q WHERE q.isActive = true AND " +
            "(LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(q.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Question> search(String searchTerm);
}