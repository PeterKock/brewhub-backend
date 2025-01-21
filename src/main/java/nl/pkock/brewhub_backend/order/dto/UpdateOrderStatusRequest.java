package nl.pkock.brewhub_backend.order.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.order.models.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}