package nl.pkock.brewhub_backend.inventory.controllers;

import nl.pkock.brewhub_backend.inventory.dto.RetailerDTO;
import nl.pkock.brewhub_backend.inventory.dto.IngredientDTO;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.inventory.models.Ingredient;
import nl.pkock.brewhub_backend.inventory.repository.IngredientRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public PublicController(UserRepository userRepository, IngredientRepository ingredientRepository) {
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping("/retailers")
    public ResponseEntity<List<RetailerDTO>> getRetailers() {
        List<User> retailers = userRepository.findByRolesContaining(UserRole.RETAILER);
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

    @GetMapping("/retailers/{retailerId}/ingredients")
    public ResponseEntity<List<IngredientDTO>> getRetailerIngredients(@PathVariable Long retailerId) {
        List<Ingredient> ingredients = ingredientRepository.findByRetailerIdAndActiveTrue(retailerId);
        List<IngredientDTO> ingredientDTOs = ingredients.stream()
                .map(ingredient -> new IngredientDTO(
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getCategory(),
                        ingredient.getQuantity(),
                        ingredient.getUnit(),
                        ingredient.getPrice(),
                        ingredient.getExpiryDate(),
                        ingredient.getLowStockThreshold(),
                        ingredient.getQuantity().compareTo(ingredient.getLowStockThreshold()) <= 0
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ingredientDTOs);
    }
}