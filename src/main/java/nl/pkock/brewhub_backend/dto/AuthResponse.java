package nl.pkock.brewhub_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public AuthResponse(String token, Long id, String email, String role, String firstName) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstName = firstName;
    }
}