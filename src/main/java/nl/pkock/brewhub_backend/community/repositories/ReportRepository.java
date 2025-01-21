package nl.pkock.brewhub_backend.community.repositories;

import nl.pkock.brewhub_backend.community.models.Report;
import nl.pkock.brewhub_backend.community.models.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByQuestionIdAndStatus(Long questionId, ReportStatus status);
    List<Report> findByAnswerIdAndStatus(Long answerId, ReportStatus status);
    boolean existsByReporterIdAndQuestionId(Long reporterId, Long questionId);
    boolean existsByReporterIdAndAnswerId(Long reporterId, Long answerId);
}