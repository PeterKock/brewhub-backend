package nl.pkock.brewhub_backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ingredients", schema = "public")
@Data
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private IngredientCategory category;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "low_stock_threshold", nullable = false)
    private BigDecimal lowStockThreshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private User retailer;
}