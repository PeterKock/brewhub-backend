package nl.pkock.brewhub_backend.controllers;

import nl.pkock.brewhub_backend.dto.NavigationDTO;
import nl.pkock.brewhub_backend.dto.MenuItemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class NavigationController {

    @GetMapping("/navigation")
    public ResponseEntity<NavigationDTO> getNavigation(Authentication authentication) {
        System.out.println("Navigation endpoint called");

        if (authentication == null) {
            System.out.println("Authentication is null");
            return ResponseEntity.ok(new NavigationDTO(new ArrayList<>()));
        }

        System.out.println("Authentication name: " + authentication.getName());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        System.out.println("User roles: " + roles);

        List<MenuItemDTO> menuItems = new ArrayList<>();

        // Check for roles with the ROLE_ prefix since that's how they're stored in the authorities
        if (roles.contains("ROLE_USER")) {
            System.out.println("Adding USER menu items");
            menuItems.addAll(List.of(
                    new MenuItemDTO("Dashboard", "/user/dashboard"),
                    new MenuItemDTO("Orders", "/user/orders"),
                    new MenuItemDTO("Community", "/community"),
                    new MenuItemDTO("Recipes", "/recipes"),
                    new MenuItemDTO("Guides", "/guides")
            ));
        }

        if (roles.contains("ROLE_RETAILER")) {
            System.out.println("Adding RETAILER menu items");
            menuItems.addAll(List.of(
                    new MenuItemDTO("Dashboard", "/retailer/dashboard"),
                    new MenuItemDTO("Orders", "/retailer/orders"),
                    new MenuItemDTO("Inventory", "/inventory"),
                    new MenuItemDTO("Community", "/community")
            ));
        }

        System.out.println("Final menu items: " + menuItems);
        return ResponseEntity.ok(new NavigationDTO(menuItems));
    }
}