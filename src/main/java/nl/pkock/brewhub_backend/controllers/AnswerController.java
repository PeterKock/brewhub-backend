package nl.pkock.brewhub_backend.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.AnswerDTO;
import nl.pkock.brewhub_backend.dto.CreateAnswerRequest;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import nl.pkock.brewhub_backend.services.AnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/community/answers")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerDTO>> getAnswersByQuestionId(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<AnswerDTO> createAnswer(
            @Valid @RequestBody CreateAnswerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(answerService.createAnswer(request, currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerDTO> updateAnswer(
            @PathVariable Long id,
            @Valid @RequestBody CreateAnswerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(answerService.updateAnswer(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        answerService.deleteAnswer(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{answerId}/accept")
    public ResponseEntity<AnswerDTO> acceptAnswer(
            @PathVariable Long answerId,
            @RequestParam Long questionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(answerService.acceptAnswer(answerId, questionId, currentUser.getId()));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<AnswerDTO> toggleVerifiedStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(answerService.toggleVerifiedStatus(id, currentUser.getId()));
    }
}