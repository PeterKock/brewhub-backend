package nl.pkock.brewhub_backend.recipe.repository;

import nl.pkock.brewhub_backend.recipe.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByDifficulty(String difficulty);
    List<Recipe> findByType(String type);
    List<Recipe> findByTitleContainingIgnoreCase(String title);
}