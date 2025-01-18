package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}