package nl.pkock.brewhub_backend.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import nl.pkock.brewhub_backend.models.IngredientCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateIngredientRequest {
    private String name;
    private IngredientCategory category;

    @Positive
    private BigDecimal quantity;

    private String unit;

    @Positive
    private BigDecimal price;

    private LocalDate expiryDate;

    @Positive
    private BigDecimal lowStockThreshold;
}