package nl.pkock.brewhub_backend.navigation.service;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.navigation.dto.MenuItemDTO;
import nl.pkock.brewhub_backend.navigation.dto.NavigationDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NavigationService {

    public NavigationDTO getNavigation(Authentication authentication) {
        if (authentication == null) {
            return new NavigationDTO(new ArrayList<>());
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<MenuItemDTO> menuItems = new ArrayList<>();

        if (roles.contains("ROLE_USER")) {
            menuItems.addAll(getUserMenuItems());
        }

        if (roles.contains("ROLE_RETAILER")) {
            menuItems.addAll(getRetailerMenuItems());
        }

        if (roles.contains("ROLE_MODERATOR")) {
            menuItems.addAll(getModeratorMenuItems());
        }

        return new NavigationDTO(menuItems);
    }

    private List<MenuItemDTO> getUserMenuItems() {
        return List.of(
                new MenuItemDTO("Dashboard", "/user/dashboard"),
                new MenuItemDTO("Orders", "/user/orders"),
                new MenuItemDTO("Community", "/community"),
                new MenuItemDTO("Recipes", "/recipes"),
                new MenuItemDTO("Guides", "/guides")
        );
    }

    private List<MenuItemDTO> getRetailerMenuItems() {
        return List.of(
                new MenuItemDTO("Dashboard", "/retailer/dashboard"),
                new MenuItemDTO("Orders", "/retailer/orders"),
                new MenuItemDTO("Inventory", "/inventory"),
                new MenuItemDTO("Community", "/community")
        );
    }

    private List<MenuItemDTO> getModeratorMenuItems() {
        return List.of(
                new MenuItemDTO("Dashboard", "/moderator/dashboard")
        );
    }
}