package nl.pkock.brewhub_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NavigationDTO {
    private List<MenuItemDTO> items;
}