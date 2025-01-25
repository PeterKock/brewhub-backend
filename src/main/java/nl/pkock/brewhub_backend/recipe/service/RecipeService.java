package nl.pkock.brewhub_backend.recipe.service;

import nl.pkock.brewhub_backend.recipe.models.Recipe;
import nl.pkock.brewhub_backend.recipe.repository.RecipeRepository;
import nl.pkock.brewhub_backend.recipe.dto.*;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<RecipeDTO> getAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        return mapToDTO(recipe);
    }

    @Transactional
    public RecipeDTO createRecipe(CreateRecipeRequest request, UserPrincipal currentUser) {
        Recipe recipe = new Recipe();
        setRecipeProperties(recipe, request);
        recipe.setCreatedBy(currentUser.getId());
        recipe.setLastModifiedBy(currentUser.getId());
        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToDTO(savedRecipe);
    }

    @Transactional
    public RecipeDTO updateRecipe(Long id, UpdateRecipeRequest request, UserPrincipal currentUser) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        setRecipeProperties(recipe, request);
        recipe.setLastModifiedBy(currentUser.getId());
        Recipe updatedRecipe = recipeRepository.save(recipe);
        return mapToDTO(updatedRecipe);
    }

    private void setRecipeProperties(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setTimeInWeeks(request.getTimeInWeeks());
        recipe.setType(request.getType());
        recipe.setAbv(request.getAbv());
        recipe.setIbu(request.getIbu());
        recipe.setIngredients(new ArrayList<>(request.getIngredients()));
        recipe.setInstructions(new ArrayList<>(request.getInstructions()));
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
    }

    public List<RecipeDTO> searchRecipes(String searchTerm) {
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(searchTerm);
        return recipes.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> getRecipesByDifficulty(String difficulty) {
        List<Recipe> recipes = recipeRepository.findByDifficulty(difficulty);
        return recipes.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> getRecipesByType(String type) {
        List<Recipe> recipes = recipeRepository.findByType(type);
        return recipes.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private RecipeDTO mapToDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        BeanUtils.copyProperties(recipe, dto);
        dto.setIngredients(new ArrayList<>(recipe.getIngredients()));
        dto.setInstructions(new ArrayList<>(recipe.getInstructions()));
        return dto;
    }
}