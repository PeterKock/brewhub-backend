package nl.pkock.brewhub_backend.controllers;

import jakarta.validation.Valid;
import nl.pkock.brewhub_backend.dto.CreateRatingRequest;
import nl.pkock.brewhub_backend.dto.RatingDTO;
import nl.pkock.brewhub_backend.models.Order;
import nl.pkock.brewhub_backend.models.OrderStatus;
import nl.pkock.brewhub_backend.models.Rating;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.repositories.RatingRepository;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.repositories.OrderRepository;
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
    private final OrderRepository orderRepository;

    public RatingController(RatingRepository ratingRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
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

    @PostMapping("/user/ratings/{retailerId}/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createRating(
            Authentication authentication,
            @PathVariable Long retailerId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateRatingRequest request) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User customer = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate order belongs to user and retailer
        if (!order.getCustomer().getId().equals(customer.getId()) ||
                !order.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Invalid order for this rating");
        }

        // Validate order status
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Can only rate delivered orders");
        }

        // Check if order already has a rating
        if (ratingRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Order already has a rating");
        }

        Rating rating = new Rating();
        rating.setRetailer(retailer);
        rating.setCustomer(customer);
        rating.setOrder(order);
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

    @GetMapping("/public/orders/{orderId}/rating")
    public ResponseEntity<RatingDTO> getOrderRating(@PathVariable Long orderId) {
        Rating rating = ratingRepository.findByOrderId(orderId)
                .orElse(null);

        if (rating == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToDTO(rating));
    }
}