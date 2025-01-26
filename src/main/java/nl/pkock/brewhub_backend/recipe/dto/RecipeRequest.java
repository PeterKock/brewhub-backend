package nl.pkock.brewhub_backend.recipe.dto;

import java.util.List;

public interface RecipeRequest {
    String getTitle();
    String getDescription();
    String getDifficulty();
    Integer getTimeInWeeks();
    String getType();
    String getAbv();
    String getIbu();
    List<String> getIngredients();
    List<String> getInstructions();
}