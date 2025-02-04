package nl.pkock.brewhub_backend.community.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.community.dto.VoteRequest;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.community.services.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/votes")
@RequiredArgsConstructor
public class VoteController {
    private final VoteService voteService;

    @PostMapping("/question")
    public ResponseEntity<Void> voteOnQuestion(
            @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        voteService.voteOnQuestion(request, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/answer")
    public ResponseEntity<Void> voteOnAnswer(
            @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        voteService.voteOnAnswer(request, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}