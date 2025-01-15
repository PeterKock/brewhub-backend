package nl.pkock.brewhub_backend.controllers;

import nl.pkock.brewhub_backend.dto.RetailerDTO;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.models.UserRole;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final UserRepository userRepository;

    public PublicController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/retailers")
    public ResponseEntity<List<RetailerDTO>> getRetailers() {
        List<User> retailers = userRepository.findByRoles(Collections.singleton(UserRole.RETAILER));
        List<RetailerDTO> retailerDTOs = retailers.stream()
                .map(retailer -> new RetailerDTO(
                        retailer.getId(),
                        retailer.getFirstName() + " " + retailer.getLastName(),
                        retailer.getLocation(),
                        retailer.getAverageRating(),
                        retailer.getTotalRatings()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(retailerDTOs);
    }
}