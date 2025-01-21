package nl.pkock.brewhub_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String role;
    private String firstName;
    private BigDecimal averageRating;
    private Integer totalRatings;

    public AuthResponse(String token, Long id, String email, String role, String firstName, BigDecimal averageRating, Integer totalRatings) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }
}