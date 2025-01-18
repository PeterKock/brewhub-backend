package nl.pkock.brewhub_backend.dto;

import lombok.Data;
import nl.pkock.brewhub_backend.models.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long retailerId;
    private String customerName;
    private String retailerName;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String notes;
    private List<OrderItemDTO> items;
    private BigDecimal retailerRating;
    private Integer retailerTotalRatings;
}