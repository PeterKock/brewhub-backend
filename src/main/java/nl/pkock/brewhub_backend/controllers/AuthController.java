package nl.pkock.brewhub_backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import nl.pkock.brewhub_backend.dto.AuthResponse;
import nl.pkock.brewhub_backend.dto.LoginRequest;
import nl.pkock.brewhub_backend.dto.SignUpRequest;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.models.UserRole;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
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

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getRoles().iterator().next().name(),
                user.getFirstName(),
                user.getAverageRating(),
                user.getTotalRatings()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email is already taken");
        }

        // Check if location is provided for retailers
        if (signUpRequest.getRole() != null &&
                signUpRequest.getRole().equalsIgnoreCase("RETAILER") &&
                (signUpRequest.getLocation() == null || signUpRequest.getLocation().trim().isEmpty())) {
            return ResponseEntity.badRequest()
                    .body("Location is required for retailers");
        }

        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setLocation(signUpRequest.getLocation()); // Set the location

        UserRole role = signUpRequest.getRole() != null && signUpRequest.getRole().equalsIgnoreCase("RETAILER")
                ? UserRole.RETAILER
                : UserRole.USER;
        user.setRoles(Collections.singleton(role));

        User savedUser = userRepository.save(user);

        // Authenticate the user after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signUpRequest.getEmail(),
                        signUpRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRoles().iterator().next().name(),
                savedUser.getFirstName(),
                savedUser.getAverageRating(),
                savedUser.getTotalRatings()
        ));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                boolean isValid = tokenProvider.validateToken(token);
                if (isValid) {
                    Long userId = tokenProvider.getUserIdFromJWT(token);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return ResponseEntity.ok(new AuthResponse(
                            token,
                            user.getId(),
                            user.getEmail(),
                            user.getRoles().iterator().next().name(),
                            user.getFirstName(),
                            user.getAverageRating(),
                            user.getTotalRatings()
                    ));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}