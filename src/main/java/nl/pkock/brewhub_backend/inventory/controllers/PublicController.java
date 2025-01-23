package nl.pkock.brewhub_backend.inventory.controllers;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.inventory.dto.RetailerDTO;
import nl.pkock.brewhub_backend.inventory.dto.IngredientDTO;
import nl.pkock.brewhub_backend.inventory.services.PublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final PublicService publicService;

    @GetMapping("/retailers")
    public ResponseEntity<List<RetailerDTO>> getRetailers() {
        return ResponseEntity.ok(publicService.getRetailers());
    }

    @GetMapping("/retailers/{retailerId}/ingredients")
    public ResponseEntity<List<IngredientDTO>> getRetailerIngredients(@PathVariable Long retailerId) {
        return ResponseEntity.ok(publicService.getRetailerIngredients(retailerId));
    }
}