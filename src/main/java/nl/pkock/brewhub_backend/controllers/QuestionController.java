package nl.pkock.brewhub_backend.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.CreateQuestionRequest;
import nl.pkock.brewhub_backend.dto.QuestionDTO;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import nl.pkock.brewhub_backend.services.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/community/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.getAllQuestions(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.getQuestionById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.createQuestion(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuestionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        questionService.deleteQuestion(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<QuestionDTO> togglePinQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.togglePin(id, currentUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionDTO>> searchQuestions(
            @RequestParam String query,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.searchQuestions(query, currentUser.getId()));
    }
}