package nl.pkock.brewhub_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import nl.pkock.brewhub_backend.models.IngredientCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateIngredientRequest {
    @NotBlank
    private String name;

    @NotNull
    private IngredientCategory category;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotBlank
    private String unit;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    @Positive
    private BigDecimal lowStockThreshold;
}