package nl.pkock.brewhub_backend.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long retailerId;
    private List<OrderItemRequest> items;
    private String notes;
}