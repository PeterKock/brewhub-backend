package nl.pkock.brewhub_backend.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
}