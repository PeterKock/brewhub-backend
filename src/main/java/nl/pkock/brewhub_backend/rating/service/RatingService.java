package nl.pkock.brewhub_backend.rating.service;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.rating.dto.CreateRatingRequest;
import nl.pkock.brewhub_backend.rating.dto.RatingDTO;
import nl.pkock.brewhub_backend.order.models.Order;
import nl.pkock.brewhub_backend.order.models.OrderStatus;
import nl.pkock.brewhub_backend.rating.model.Rating;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.order.repository.OrderRepository;
import nl.pkock.brewhub_backend.rating.repository.RatingRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private RatingDTO convertToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setCustomerName(rating.getCustomer().getFirstName() + " " + rating.getCustomer().getLastName());
        dto.setScore(rating.getScore());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        if (rating.getOrder() != null) {
            dto.setOrderId(rating.getOrder().getId());
        }
        return dto;
    }

    @Transactional
    public RatingDTO createRating(Long userId, Long retailerId, Long orderId, CreateRatingRequest request) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        validateOrderForRating(order, customer.getId(), retailerId);

        Rating rating = new Rating();
        rating.setRetailer(retailer);
        rating.setCustomer(customer);
        rating.setOrder(order);
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());

        Rating savedRating = ratingRepository.save(rating);
        updateRetailerRating(retailer);

        return convertToDTO(savedRating);
    }

    private void validateOrderForRating(Order order, Long customerId, Long retailerId) {
        if (!order.getCustomer().getId().equals(customerId) ||
                !order.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Invalid order for this rating");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Can only rate delivered orders");
        }

        if (ratingRepository.findByOrderId(order.getId()).isPresent()) {
            throw new RuntimeException("Order already has a rating");
        }
    }

    private void updateRetailerRating(User retailer) {
        Double avgRating = ratingRepository.findAverageRatingByRetailerId(retailer.getId())
                .orElse(0.0);

        retailer.setAverageRating(BigDecimal.valueOf(avgRating));
        retailer.setTotalRatings(retailer.getTotalRatings() == null ? 1 : retailer.getTotalRatings() + 1);
        userRepository.save(retailer);
    }

    @Transactional(readOnly = true)
    public List<RatingDTO> getRetailerRatings(Long retailerId) {
        return ratingRepository.findByRetailerId(retailerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getRetailerAverageRating(Long retailerId) {
        return userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"))
                .getAverageRating();
    }

    @Transactional(readOnly = true)
    public RatingDTO getOrderRating(Long orderId) {
        return ratingRepository.findByOrderId(orderId)
                .map(this::convertToDTO)
                .orElse(null);
    }
}