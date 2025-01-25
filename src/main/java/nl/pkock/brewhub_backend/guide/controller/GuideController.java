package nl.pkock.brewhub_backend.guide.controller;

import nl.pkock.brewhub_backend.guide.dto.*;
import nl.pkock.brewhub_backend.guide.service.GuideService;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/guides")
public class GuideController {
    private final GuideService guideService;

    @Autowired
    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @GetMapping
    public List<GuideDTO> getAllGuides() {
        return guideService.getAllGuides();
    }

    @GetMapping("/{id}")
    public GuideDTO getGuideById(@PathVariable Long id) {
        return guideService.getGuideById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public GuideDTO createGuide(
            @Valid @RequestBody CreateGuideRequest request,
            Authentication authentication
    ) {
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        return guideService.createGuide(request, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public GuideDTO updateGuide(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGuideRequest request,
            Authentication authentication
    ) {
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        return guideService.updateGuide(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> deleteGuide(@PathVariable Long id) {
        guideService.deleteGuide(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public List<GuideDTO> searchGuides(@RequestParam String term) {
        return guideService.searchGuides(term);
    }

    @GetMapping("/category/{category}")
    public List<GuideDTO> getGuidesByCategory(@PathVariable String category) {
        return guideService.getGuidesByCategory(category);
    }
}