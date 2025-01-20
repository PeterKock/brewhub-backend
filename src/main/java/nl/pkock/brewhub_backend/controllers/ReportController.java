package nl.pkock.brewhub_backend.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.CreateReportRequest;
import nl.pkock.brewhub_backend.dto.ReportDTO;
import nl.pkock.brewhub_backend.models.ReportStatus;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import nl.pkock.brewhub_backend.services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/community/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportDTO> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.createReport(request, currentUser.getId()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReportDTO>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReportDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestParam ReportStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, status, currentUser.getId()));
    }
}