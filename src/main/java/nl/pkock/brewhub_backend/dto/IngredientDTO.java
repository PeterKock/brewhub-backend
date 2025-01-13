package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.IngredientCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngredientDTO {
    private Long id;
    private String name;
    private IngredientCategory category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private LocalDate expiryDate;
    private BigDecimal lowStockThreshold;
    private boolean lowStock;
}