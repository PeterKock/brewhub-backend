package nl.pkock.brewhub_backend.recipe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.pkock.brewhub_backend.auth.security.WithMockCustomUser;
import nl.pkock.brewhub_backend.recipe.dto.CreateRecipeRequest;
import nl.pkock.brewhub_backend.recipe.models.Recipe;
import nl.pkock.brewhub_backend.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    private CreateRecipeRequest testRecipeRequest;

    @BeforeEach
    void setUp() {
        testRecipeRequest = new CreateRecipeRequest();
        testRecipeRequest.setTitle("Test IPA");
        testRecipeRequest.setDescription("A test IPA recipe");
        testRecipeRequest.setDifficulty("Intermediate");
        testRecipeRequest.setTimeInWeeks(4);
        testRecipeRequest.setType("Ale");
        testRecipeRequest.setAbv("6.5%");
        testRecipeRequest.setIbu("65");
        testRecipeRequest.setIngredients(List.of("Malt", "Hops", "Yeast"));
        testRecipeRequest.setInstructions(List.of("Step 1", "Step 2", "Step 3"));
    }

    @Test
    void getAllRecipes_ShouldReturnAllRecipes() throws Exception {
        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockCustomUser
    void createRecipe_WithValidData_ShouldCreateRecipe() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRecipeRequest)));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(testRecipeRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(testRecipeRequest.getDescription()));
    }

    @Test
    void searchRecipes_ShouldReturnMatchingRecipes() throws Exception {
        String searchTerm = "IPA";

        mockMvc.perform(get("/api/recipes/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRecipesByDifficulty_ShouldReturnRecipesWithMatchingDifficulty() throws Exception {
        String difficulty = "Beginner";

        mockMvc.perform(get("/api/recipes/difficulty/{difficulty}", difficulty))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockCustomUser
    void deleteRecipe_WithModeratorRole_ShouldDeleteRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle(testRecipeRequest.getTitle());
        recipe.setDescription(testRecipeRequest.getDescription());
        recipe.setDifficulty(testRecipeRequest.getDifficulty());
        recipe.setTimeInWeeks(testRecipeRequest.getTimeInWeeks());
        recipe.setType(testRecipeRequest.getType());
        recipe.setAbv(testRecipeRequest.getAbv());
        recipe.setIbu(testRecipeRequest.getIbu());
        recipe.setIngredients(testRecipeRequest.getIngredients());
        recipe.setInstructions(testRecipeRequest.getInstructions());
        Recipe savedRecipe = recipeRepository.save(recipe);

        mockMvc.perform(delete("/api/recipes/{id}", savedRecipe.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRecipe_WithoutModeratorRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/recipes/1"))
                .andExpect(status().isForbidden());
    }
}