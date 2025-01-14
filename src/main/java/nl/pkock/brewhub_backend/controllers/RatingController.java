package nl.pkock.brewhub_backend.controllers;

import jakarta.validation.Valid;
import nl.pkock.brewhub_backend.dto.CreateRatingRequest;
import nl.pkock.brewhub_backend.dto.RatingDTO;
import nl.pkock.brewhub_backend.models.Rating;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.repositories.RatingRepository;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RatingController {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingController(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    private RatingDTO convertToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setCustomerName(rating.getCustomer().getFirstName() + " " + rating.getCustomer().getLastName());
        dto.setScore(rating.getScore());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        return dto;
    }

    @PostMapping("/user/ratings/{retailerId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createRating(
            Authentication authentication,
            @PathVariable Long retailerId,
            @Valid @RequestBody CreateRatingRequest request) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User customer = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        // Check if user has already rated this retailer
        ratingRepository.findByRetailerIdAndCustomerId(retailerId, customer.getId())
                .ifPresent(r -> {
                    throw new RuntimeException("You have already rated this retailer");
                });

        Rating rating = new Rating();
        rating.setRetailer(retailer);
        rating.setCustomer(customer);
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());

        Rating savedRating = ratingRepository.save(rating);

        // Update retailer's average rating
        Double avgRating = ratingRepository.findAverageRatingByRetailerId(retailerId)
                .orElse(0.0);

        retailer.setAverageRating(BigDecimal.valueOf(avgRating));
        retailer.setTotalRatings(retailer.getTotalRatings() == null ? 1 : retailer.getTotalRatings() + 1);
        userRepository.save(retailer);

        return ResponseEntity.ok(convertToDTO(savedRating));
    }

    @GetMapping("/public/retailers/{retailerId}/ratings")
    public ResponseEntity<List<RatingDTO>> getRetailerRatings(@PathVariable Long retailerId) {
        List<Rating> ratings = ratingRepository.findByRetailerId(retailerId);
        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ratingDTOs);
    }

    @GetMapping("/public/retailers/{retailerId}/average-rating")
    public ResponseEntity<BigDecimal> getRetailerAverageRating(@PathVariable Long retailerId) {
        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));
        return ResponseEntity.ok(retailer.getAverageRating());
    }
}