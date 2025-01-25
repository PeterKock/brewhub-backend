package nl.pkock.brewhub_backend.recipe.service;

import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.recipe.dto.CreateRecipeRequest;
import nl.pkock.brewhub_backend.recipe.dto.RecipeDTO;
import nl.pkock.brewhub_backend.recipe.dto.UpdateRecipeRequest;
import nl.pkock.brewhub_backend.recipe.models.Recipe;
import nl.pkock.brewhub_backend.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe testRecipe;
    private UserPrincipal testUser;
    private final List<String> testIngredients = List.of("Malt", "Hops", "Yeast");
    private final List<String> testInstructions = List.of("Step 1", "Step 2", "Step 3");

    @BeforeEach
    void setUp() {
        testRecipe = createTestRecipe();
        testUser = new UserPrincipal(1L, "test@test.com", "password", List.of());
    }

    private Recipe createTestRecipe() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Test IPA");
        recipe.setDescription("A test IPA recipe");
        recipe.setDifficulty("Intermediate");
        recipe.setTimeInWeeks(4);
        recipe.setType("Ale");
        recipe.setAbv("6.5%");
        recipe.setIbu("65");
        recipe.setIngredients(testIngredients);
        recipe.setInstructions(testInstructions);
        recipe.setCreatedBy(1L);
        recipe.setLastModifiedBy(1L);
        return recipe;
    }

    private CreateRecipeRequest createTestRequest() {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setTitle(testRecipe.getTitle());
        request.setDescription(testRecipe.getDescription());
        request.setDifficulty(testRecipe.getDifficulty());
        request.setTimeInWeeks(testRecipe.getTimeInWeeks());
        request.setType(testRecipe.getType());
        request.setAbv(testRecipe.getAbv());
        request.setIbu(testRecipe.getIbu());
        request.setIngredients(testIngredients);
        request.setInstructions(testInstructions);
        return request;
    }

    private UpdateRecipeRequest createUpdateRequest() {
        UpdateRecipeRequest request = new UpdateRecipeRequest();
        request.setTitle("Updated Recipe");
        request.setDescription(testRecipe.getDescription());
        request.setDifficulty(testRecipe.getDifficulty());
        request.setTimeInWeeks(testRecipe.getTimeInWeeks());
        request.setType(testRecipe.getType());
        request.setAbv(testRecipe.getAbv());
        request.setIbu(testRecipe.getIbu());
        request.setIngredients(testIngredients);
        request.setInstructions(testInstructions);
        return request;
    }

    @Test
    void getAllRecipes_ShouldReturnListOfRecipes() {
        when(recipeRepository.findAll()).thenReturn(List.of(testRecipe));

        List<RecipeDTO> result = recipeService.getAllRecipes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipe.getTitle(), result.get(0).getTitle());
        verify(recipeRepository).findAll();
    }

    @Test
    void getRecipeById_WhenRecipeExists_ShouldReturnRecipe() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));

        RecipeDTO result = recipeService.getRecipeById(1L);

        assertNotNull(result);
        assertEquals(testRecipe.getTitle(), result.getTitle());
        verify(recipeRepository).findById(1L);
    }

    @Test
    void createRecipe_ShouldSaveAndReturnRecipe() {
        CreateRecipeRequest request = createTestRequest();
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        RecipeDTO result = recipeService.createRecipe(request, testUser);

        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void updateRecipe_WhenRecipeExists_ShouldUpdateAndReturnRecipe() {
        UpdateRecipeRequest request = createUpdateRequest();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        RecipeDTO result = recipeService.updateRecipe(1L, request, testUser);

        assertNotNull(result);
        verify(recipeRepository).findById(1L);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void searchRecipes_ShouldReturnMatchingRecipes() {
        String searchTerm = "IPA";
        when(recipeRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(List.of(testRecipe));

        List<RecipeDTO> result = recipeService.searchRecipes(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipeRepository).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void getRecipesByDifficulty_ShouldReturnRecipesWithMatchingDifficulty() {
        String difficulty = "Intermediate";
        when(recipeRepository.findByDifficulty(difficulty))
                .thenReturn(List.of(testRecipe));

        List<RecipeDTO> result = recipeService.getRecipesByDifficulty(difficulty);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(difficulty, result.get(0).getDifficulty());
        verify(recipeRepository).findByDifficulty(difficulty);
    }

    @Test
    void getRecipesByType_ShouldReturnRecipesWithMatchingType() {
        String type = "Ale";
        when(recipeRepository.findByType(type))
                .thenReturn(List.of(testRecipe));

        List<RecipeDTO> result = recipeService.getRecipesByType(type);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getType());
        verify(recipeRepository).findByType(type);
    }
}