package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.active = true ORDER BY q.createdAt DESC")
    List<Question> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT q FROM Question q WHERE q.active = true AND q.pinned = true")
    List<Question> findByActiveTrueAndPinnedTrue();

    @Query("SELECT q FROM Question q WHERE q.active = true ORDER BY q.createdAt DESC")
    List<Question> findTop10ByActiveTrueOrderByCreatedAtDesc();

    @Query(value = "SELECT q.* FROM questions q " +
            "LEFT JOIN votes v ON q.id = v.question_id " +
            "WHERE q.is_active = true " +
            "GROUP BY q.id " +
            "ORDER BY SUM(CASE WHEN v.type = 'UPVOTE' THEN 1 WHEN v.type = 'DOWNVOTE' THEN -1 ELSE 0 END) DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Question> findMostUpvotedQuestions();

    @Query("SELECT COUNT(q) FROM Question q WHERE q.active = true")
    long countByActiveTrue();

    @Query("SELECT q FROM Question q WHERE q.active = true AND " +
            "(LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(q.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Question> search(@Param("query") String query);
}