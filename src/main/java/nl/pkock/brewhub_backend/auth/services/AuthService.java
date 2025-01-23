package nl.pkock.brewhub_backend.auth.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.auth.dto.AuthResponse;
import nl.pkock.brewhub_backend.auth.dto.LoginRequest;
import nl.pkock.brewhub_backend.auth.dto.SignUpRequest;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.auth.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getRoles().iterator().next().name(),
                user.getFirstName(),
                user.getAverageRating(),
                user.getTotalRatings()
        );
    }

    @Transactional
    public AuthResponse registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        validateRetailerSignup(signUpRequest);

        User user = createUserFromRequest(signUpRequest);
        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signUpRequest.getEmail(),
                        signUpRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRoles().iterator().next().name(),
                savedUser.getFirstName(),
                savedUser.getAverageRating(),
                savedUser.getTotalRatings()
        );
    }

    private void validateRetailerSignup(SignUpRequest request) {
        if (request.getRole() != null &&
                request.getRole().equalsIgnoreCase("RETAILER") &&
                (request.getLocation() == null || request.getLocation().trim().isEmpty())) {
            throw new RuntimeException("Location is required for retailers");
        }
    }

    private User createUserFromRequest(SignUpRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLocation(request.getLocation());
        user.setRoles(Collections.singleton(determineUserRole(request)));
        return user;
    }

    private UserRole determineUserRole(SignUpRequest request) {
        if (request.getRole() != null) {
            return switch (request.getRole().toUpperCase()) {
                case "RETAILER" -> UserRole.RETAILER;
                case "MODERATOR" -> UserRole.MODERATOR;
                default -> UserRole.USER;
            };
        }
        return UserRole.USER;
    }

    @Transactional(readOnly = true)
    public AuthResponse verifyToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromJWT(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                return new AuthResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getRoles().iterator().next().name(),
                        user.getFirstName(),
                        user.getAverageRating(),
                        user.getTotalRatings()
                );
            }
        }
        throw new RuntimeException("Invalid token");
    }
}