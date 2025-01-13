package nl.pkock.brewhub_backend.controllers;

import jakarta.validation.Valid;
import nl.pkock.brewhub_backend.dto.CreateIngredientRequest;
import nl.pkock.brewhub_backend.dto.IngredientDTO;
import nl.pkock.brewhub_backend.dto.UpdateIngredientRequest;
import nl.pkock.brewhub_backend.models.Ingredient;
import nl.pkock.brewhub_backend.models.IngredientCategory;
import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.repositories.IngredientRepository;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/retailer/inventory")
@PreAuthorize("hasRole('RETAILER')")
public class InventoryController {

    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    public InventoryController(IngredientRepository ingredientRepository, UserRepository userRepository) {
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
    }

    private void copyIngredientProperties(Ingredient source, IngredientDTO target) {
        target.setName(source.getName());
        target.setCategory(source.getCategory());
        target.setQuantity(source.getQuantity());
        target.setUnit(source.getUnit());
        target.setPrice(source.getPrice());
        target.setExpiryDate(source.getExpiryDate());
        target.setLowStockThreshold(source.getLowStockThreshold());
    }

    private void copyIngredientProperties(CreateIngredientRequest source, Ingredient target) {
        target.setName(source.getName());
        target.setCategory(source.getCategory());
        target.setQuantity(source.getQuantity());
        target.setUnit(source.getUnit());
        target.setPrice(source.getPrice());
        target.setExpiryDate(source.getExpiryDate());
        target.setLowStockThreshold(source.getLowStockThreshold());
    }

    private IngredientDTO convertToDTO(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        copyIngredientProperties(ingredient, dto);
        dto.setLowStock(ingredient.getQuantity().compareTo(ingredient.getLowStockThreshold()) <= 0);
        return dto;
    }

    private List<IngredientDTO> convertToDTOList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Ingredient getAndVerifyIngredient(Authentication authentication, Long id) {
        Long retailerId = ((UserPrincipal) authentication.getPrincipal()).getId();
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        if (!ingredient.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized access to ingredient");
        }

        return ingredient;
    }

    private Long getRetailerId(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) IngredientCategory category) {

        Long retailerId = getRetailerId(authentication);
        List<Ingredient> ingredients;

        if (search != null && !search.trim().isEmpty()) {
            ingredients = ingredientRepository.searchIngredients(retailerId, search.trim());
        } else if (category != null) {
            ingredients = ingredientRepository.findByRetailerIdAndCategory(retailerId, category);
        } else {
            ingredients = ingredientRepository.findByRetailerId(retailerId);
        }

        return ResponseEntity.ok(convertToDTOList(ingredients));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientDTO>> getLowStockIngredients(Authentication authentication) {
        Long retailerId = getRetailerId(authentication);
        List<Ingredient> ingredients = ingredientRepository.findLowStockIngredients(retailerId);
        return ResponseEntity.ok(convertToDTOList(ingredients));
    }

    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(
            Authentication authentication,
            @Valid @RequestBody CreateIngredientRequest request) {

        Long retailerId = getRetailerId(authentication);
        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Ingredient ingredient = new Ingredient();
        copyIngredientProperties(request, ingredient);
        ingredient.setRetailer(retailer);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return ResponseEntity.ok(convertToDTO(savedIngredient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request) {

        Ingredient ingredient = getAndVerifyIngredient(authentication, id);

        // Update only non-null fields
        if (request.getName() != null) {
            ingredient.setName(request.getName());
        }
        if (request.getCategory() != null) {
            ingredient.setCategory(request.getCategory());
        }
        if (request.getQuantity() != null) {
            ingredient.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            ingredient.setUnit(request.getUnit());
        }
        if (request.getPrice() != null) {
            ingredient.setPrice(request.getPrice());
        }
        if (request.getExpiryDate() != null) {
            ingredient.setExpiryDate(request.getExpiryDate());
        }
        if (request.getLowStockThreshold() != null) {
            ingredient.setLowStockThreshold(request.getLowStockThreshold());
        }

        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        return ResponseEntity.ok(convertToDTO(updatedIngredient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIngredient(
            Authentication authentication,
            @PathVariable Long id) {

        Ingredient ingredient = getAndVerifyIngredient(authentication, id);
        ingredientRepository.delete(ingredient);
        return ResponseEntity.ok().build();
    }
}