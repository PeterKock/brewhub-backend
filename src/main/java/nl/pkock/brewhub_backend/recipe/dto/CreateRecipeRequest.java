package nl.pkock.brewhub_backend.recipe.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRecipeRequest implements RecipeRequest {
    private String title;
    private String description;
    private String difficulty;
    private Integer timeInWeeks;
    private String type;
    private String abv;
    private String ibu;
    private List<String> ingredients;
    private List<String> instructions;
}