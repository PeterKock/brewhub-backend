package nl.pkock.brewhub_backend.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.dto.CommunityPageResponse;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import nl.pkock.brewhub_backend.services.CommunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<CommunityPageResponse> getCommunityPageData(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(communityService.getCommunityPageData(currentUser.getId()));
    }
}