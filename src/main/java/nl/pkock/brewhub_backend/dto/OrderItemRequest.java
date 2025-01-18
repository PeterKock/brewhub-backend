package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Long ingredientId;
    private BigDecimal quantity;
}