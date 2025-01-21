package nl.pkock.brewhub_backend.inventory.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngredientCsvDTO {
    @CsvBindByName(column = "Name")
    private String name;

    @CsvBindByName(column = "Category")
    private String category;

    @CsvBindByName(column = "Quantity")
    private BigDecimal quantity;

    @CsvBindByName(column = "Unit")
    private String unit;

    @CsvBindByName(column = "Price")
    private BigDecimal price;

    @CsvBindByName(column = "Expiry Date")
    @CsvDate(value = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @CsvBindByName(column = "Low Stock Threshold")
    private BigDecimal lowStockThreshold;
}