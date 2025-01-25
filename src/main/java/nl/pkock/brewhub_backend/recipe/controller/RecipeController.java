package nl.pkock.brewhub_backend.recipe.controller;

import nl.pkock.brewhub_backend.recipe.dto.*;
import nl.pkock.brewhub_backend.recipe.service.RecipeService;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeDTO> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public RecipeDTO getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public RecipeDTO createRecipe(
            @Valid @RequestBody CreateRecipeRequest request,
            Authentication authentication
    ) {
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        return recipeService.createRecipe(request, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public RecipeDTO updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecipeRequest request,
            Authentication authentication
    ) {
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        return recipeService.updateRecipe(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public List<RecipeDTO> searchRecipes(@RequestParam String term) {
        return recipeService.searchRecipes(term);
    }

    @GetMapping("/difficulty/{difficulty}")
    public List<RecipeDTO> getRecipesByDifficulty(@PathVariable String difficulty) {
        return recipeService.getRecipesByDifficulty(difficulty);
    }

    @GetMapping("/type/{type}")
    public List<RecipeDTO> getRecipesByType(@PathVariable String type) {
        return recipeService.getRecipesByType(type);
    }
}