package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Ingredient;
import nl.pkock.brewhub_backend.models.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByRetailerIdAndActiveTrue(Long retailerId);
    List<Ingredient> findByRetailerIdAndActiveFalse(Long retailerId);

    @Query("SELECT i FROM Ingredient i WHERE i.retailer.id = ?1 AND i.active = true AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(i.category) LIKE LOWER(CONCAT('%', ?2, '%')))")
    List<Ingredient> searchIngredients(Long retailerId, String searchTerm);

    // Update other methods to include active = true
    List<Ingredient> findByRetailerIdAndCategoryAndActiveTrue(Long retailerId, IngredientCategory category);

    @Query("SELECT i FROM Ingredient i WHERE i.retailer.id = ?1 AND i.active = true AND i.quantity <= i.lowStockThreshold")
    List<Ingredient> findLowStockIngredients(Long retailerId);

    @Query("SELECT i FROM Ingredient i WHERE i.retailer.id = ?1 AND i.active = false AND " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR " +
            "LOWER(i.category) LIKE LOWER(CONCAT('%', ?2, '%')))")
    List<Ingredient> searchDeletedIngredients(Long retailerId, String searchTerm);

    List<Ingredient> findByRetailerIdAndCategoryAndActiveFalse(Long retailerId, IngredientCategory category);
}