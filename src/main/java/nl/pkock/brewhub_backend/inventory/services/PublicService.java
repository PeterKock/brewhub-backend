package nl.pkock.brewhub_backend.inventory.services;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.inventory.dto.RetailerDTO;
import nl.pkock.brewhub_backend.inventory.dto.IngredientDTO;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.inventory.models.Ingredient;
import nl.pkock.brewhub_backend.inventory.repository.IngredientRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<RetailerDTO> getRetailers() {
        List<User> retailers = userRepository.findByRolesContaining(UserRole.RETAILER);
        return retailers.stream()
                .map(retailer -> new RetailerDTO(
                        retailer.getId(),
                        retailer.getFirstName() + " " + retailer.getLastName(),
                        retailer.getLocation(),
                        retailer.getAverageRating(),
                        retailer.getTotalRatings()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IngredientDTO> getRetailerIngredients(Long retailerId) {
        List<Ingredient> ingredients = ingredientRepository.findByRetailerIdAndActiveTrue(retailerId);
        return ingredients.stream()
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
    }
}