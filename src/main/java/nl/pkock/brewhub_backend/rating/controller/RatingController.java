package nl.pkock.brewhub_backend.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.rating.dto.CreateRatingRequest;
import nl.pkock.brewhub_backend.rating.dto.RatingDTO;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.rating.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/user/ratings/{retailerId}/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createRating(
            Authentication authentication,
            @PathVariable Long retailerId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateRatingRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        RatingDTO rating = ratingService.createRating(userPrincipal.getId(), retailerId, orderId, request);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/public/retailers/{retailerId}/ratings")
    public ResponseEntity<List<RatingDTO>> getRetailerRatings(@PathVariable Long retailerId) {
        return ResponseEntity.ok(ratingService.getRetailerRatings(retailerId));
    }

    @GetMapping("/public/retailers/{retailerId}/average-rating")
    public ResponseEntity<BigDecimal> getRetailerAverageRating(@PathVariable Long retailerId) {
        return ResponseEntity.ok(ratingService.getRetailerAverageRating(retailerId));
    }

    @GetMapping("/public/orders/{orderId}/rating")
    public ResponseEntity<RatingDTO> getOrderRating(@PathVariable Long orderId) {
        RatingDTO rating = ratingService.getOrderRating(orderId);
        return rating != null ? ResponseEntity.ok(rating) : ResponseEntity.notFound().build();
    }
}