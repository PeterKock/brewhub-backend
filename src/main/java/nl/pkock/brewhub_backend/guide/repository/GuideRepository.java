package nl.pkock.brewhub_backend.guide.repository;

import nl.pkock.brewhub_backend.guide.models.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    List<Guide> findByCategory(String category);
    List<Guide> findByTitleContainingIgnoreCase(String title);
}