package nl.pkock.brewhub_backend.recipe.service;

import nl.pkock.brewhub_backend.recipe.models.Recipe;
import nl.pkock.brewhub_backend.recipe.repository.RecipeRepository;
import nl.pkock.brewhub_backend.recipe.dto.*;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
    }

    @Transactional
    public Recipe createRecipe(CreateRecipeRequest request, UserPrincipal currentUser) {
        Recipe recipe = new Recipe();
        setRecipeProperties(recipe, request);
        recipe.setCreatedBy(currentUser.getId());
        recipe.setLastModifiedBy(currentUser.getId());
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateRecipe(Long id, UpdateRecipeRequest request, UserPrincipal currentUser) {
        Recipe recipe = getRecipeById(id);
        setRecipeProperties(recipe, request);
        recipe.setLastModifiedBy(currentUser.getId());
        return recipeRepository.save(recipe);
    }

    private void setRecipeProperties(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setTimeInWeeks(request.getTimeInWeeks());
        recipe.setType(request.getType());
        recipe.setAbv(request.getAbv());
        recipe.setIbu(request.getIbu());
        recipe.setIngredients(request.getIngredients());
        recipe.setInstructions(request.getInstructions());
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = getRecipeById(id);
        recipeRepository.delete(recipe);
    }

    public List<Recipe> searchRecipes(String searchTerm) {
        return recipeRepository.findByTitleContainingIgnoreCase(searchTerm);
    }

    public List<Recipe> getRecipesByDifficulty(String difficulty) {
        return recipeRepository.findByDifficulty(difficulty);
    }

    public List<Recipe> getRecipesByType(String type) {
        return recipeRepository.findByType(type);
    }
}