package nl.pkock.brewhub_backend.navigation.controller;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.navigation.dto.NavigationDTO;
import nl.pkock.brewhub_backend.navigation.service.NavigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class NavigationController {
    private final NavigationService navigationService;

    @GetMapping("/navigation")
    public ResponseEntity<NavigationDTO> getNavigation(Authentication authentication) {
        return ResponseEntity.ok(navigationService.getNavigation(authentication));
    }
}