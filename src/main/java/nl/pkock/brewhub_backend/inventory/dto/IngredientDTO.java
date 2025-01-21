package nl.pkock.brewhub_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.pkock.brewhub_backend.inventory.models.IngredientCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDTO {
    private Long id;
    private String name;
    private IngredientCategory category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private LocalDate expiryDate;
    private BigDecimal lowStockThreshold;
    private boolean isLowStock;
}