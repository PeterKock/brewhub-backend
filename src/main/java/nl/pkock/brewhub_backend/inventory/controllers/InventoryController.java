package nl.pkock.brewhub_backend.inventory.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.inventory.dto.*;
import nl.pkock.brewhub_backend.inventory.models.IngredientCategory;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.inventory.services.InventoryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/retailer/inventory")
@PreAuthorize("hasRole('RETAILER')")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    private Long getRetailerId(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) IngredientCategory category) {
        return ResponseEntity.ok(inventoryService.getAllIngredients(getRetailerId(authentication), search, category));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientDTO>> getLowStockIngredients(Authentication authentication) {
        return ResponseEntity.ok(inventoryService.getLowStockIngredients(getRetailerId(authentication)));
    }

    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(
            Authentication authentication,
            @Valid @RequestBody CreateIngredientRequest request) {
        return ResponseEntity.ok(inventoryService.createIngredient(getRetailerId(authentication), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngredientRequest request) {
        return ResponseEntity.ok(inventoryService.updateIngredient(getRetailerId(authentication), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIngredient(
            Authentication authentication,
            @PathVariable Long id) {
        inventoryService.deleteIngredient(getRetailerId(authentication), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportInventory(Authentication authentication) {
        String csvContent = inventoryService.exportInventory(getRetailerId(authentication));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=inventory_" + LocalDate.now() + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<IngredientDTO>> getDeletedIngredients(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) IngredientCategory category) {
        return ResponseEntity.ok(inventoryService.getDeletedIngredients(getRetailerId(authentication), search, category));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<IngredientDTO> restoreIngredient(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.restoreIngredient(getRetailerId(authentication), id));
    }

    @PostMapping("/import")
    public ResponseEntity<?> importInventory(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(inventoryService.importInventory(getRetailerId(authentication), file));
    }
}