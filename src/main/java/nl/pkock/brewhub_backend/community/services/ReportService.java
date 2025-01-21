package nl.pkock.brewhub_backend.community.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.dto.CreateReportRequest;
import nl.pkock.brewhub_backend.community.dto.ReportDTO;
import nl.pkock.brewhub_backend.community.exceptions.DuplicateReportException;
import nl.pkock.brewhub_backend.community.models.Answer;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.Report;
import nl.pkock.brewhub_backend.community.models.ReportStatus;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.community.repositories.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportDTO createReport(CreateReportRequest request, Long reporterId) {
        // Check for existing reports
        if (request.getQuestionId() != null &&
                reportRepository.existsByReporterIdAndQuestionId(reporterId, request.getQuestionId())) {
            throw new DuplicateReportException("You have already reported this question");
        }
        if (request.getAnswerId() != null &&
                reportRepository.existsByReporterIdAndAnswerId(reporterId, request.getAnswerId())) {
            throw new DuplicateReportException("You have already reported this answer");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus(ReportStatus.PENDING);

        if (request.getQuestionId() != null) {
            Question question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            report.setQuestion(question);
        } else if (request.getAnswerId() != null) {
            Answer answer = answerRepository.findById(request.getAnswerId())
                    .orElseThrow(() -> new RuntimeException("Answer not found"));
            report.setAnswer(answer);
        }

        Report savedReport = reportRepository.save(report);
        return mapToDTO(savedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private boolean isModeratorUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().contains(UserRole.MODERATOR))
                .orElse(false);
    }

    @Transactional
    public ReportDTO updateReportStatus(Long reportId, ReportStatus newStatus, Long moderatorId) {
        if (!isModeratorUser(moderatorId)) {
            throw new RuntimeException("Only moderators can update report status");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(newStatus);
        if (newStatus != ReportStatus.PENDING) {
            report.setResolvedAt(LocalDateTime.now());
        }

        if (newStatus == ReportStatus.APPROVED) {
            if (report.getQuestion() != null) {
                Question question = report.getQuestion();
                question.setActive(false);
                questionRepository.save(question);
            } else if (report.getAnswer() != null) {
                Answer answer = report.getAnswer();
                answer.setActive(false);
                answerRepository.save(answer);
            }
        }

        Report updatedReport = reportRepository.save(report);
        return mapToDTO(updatedReport);
    }


    private ReportDTO mapToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getFirstName() + " " + report.getReporter().getLastName());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }
}