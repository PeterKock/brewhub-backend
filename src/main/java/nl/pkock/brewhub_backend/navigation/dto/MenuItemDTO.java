package nl.pkock.brewhub_backend.navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItemDTO {
    private String label;
    private String path;
}