package nl.pkock.brewhub_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetailerDTO {
    private Long id;
    private String name;
    private String location;
    private BigDecimal averageRating;
    private Integer totalRatings;
}