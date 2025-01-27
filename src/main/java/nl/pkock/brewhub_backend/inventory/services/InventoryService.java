package nl.pkock.brewhub_backend.inventory.services;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.inventory.dto.*;
import nl.pkock.brewhub_backend.inventory.models.Ingredient;
import nl.pkock.brewhub_backend.inventory.models.IngredientCategory;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.inventory.repository.IngredientRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    private void copyIngredientProperties(Ingredient source, IngredientDTO target) {
        target.setName(source.getName());
        target.setCategory(source.getCategory());
        target.setQuantity(source.getQuantity());
        target.setUnit(source.getUnit());
        target.setPrice(source.getPrice());
        target.setExpiryDate(source.getExpiryDate());
        target.setLowStockThreshold(source.getLowStockThreshold());
    }

    private void copyIngredientProperties(CreateIngredientRequest source, Ingredient target) {
        target.setName(source.getName());
        target.setCategory(source.getCategory());
        target.setQuantity(source.getQuantity());
        target.setUnit(source.getUnit());
        target.setPrice(source.getPrice());
        target.setExpiryDate(source.getExpiryDate());
        target.setLowStockThreshold(source.getLowStockThreshold());
    }

    private IngredientDTO convertToDTO(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        copyIngredientProperties(ingredient, dto);
        dto.setLowStock(ingredient.getQuantity().compareTo(ingredient.getLowStockThreshold()) <= 0);
        return dto;
    }

    private List<IngredientDTO> convertToDTOList(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IngredientDTO> getAllIngredients(Long retailerId, String search, IngredientCategory category) {
        List<Ingredient> ingredients;

        if (search != null && !search.trim().isEmpty()) {
            ingredients = ingredientRepository.searchIngredients(retailerId, search.trim());
        } else if (category != null) {
            ingredients = ingredientRepository.findByRetailerIdAndCategoryAndActiveTrue(retailerId, category);
        } else {
            ingredients = ingredientRepository.findByRetailerIdAndActiveTrue(retailerId);
        }

        return convertToDTOList(ingredients);
    }

    @Transactional(readOnly = true)
    public List<IngredientDTO> getLowStockIngredients(Long retailerId) {
        return convertToDTOList(ingredientRepository.findLowStockIngredients(retailerId));
    }

    @Transactional
    public IngredientDTO createIngredient(Long retailerId, CreateIngredientRequest request) {
        User retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Ingredient ingredient = new Ingredient();
        copyIngredientProperties(request, ingredient);
        ingredient.setRetailer(retailer);
        ingredient.setActive(true);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return convertToDTO(savedIngredient);
    }

    @Transactional
    public IngredientDTO updateIngredient(Long retailerId, Long id, UpdateIngredientRequest request) {
        Ingredient ingredient = verifyIngredientAccess(retailerId, id);
        updateIngredientFields(ingredient, request);
        return convertToDTO(ingredientRepository.save(ingredient));
    }

    private void updateIngredientFields(Ingredient ingredient, UpdateIngredientRequest request) {
        if (request.getName() != null) ingredient.setName(request.getName());
        if (request.getCategory() != null) ingredient.setCategory(request.getCategory());
        if (request.getQuantity() != null) ingredient.setQuantity(request.getQuantity());
        if (request.getUnit() != null) ingredient.setUnit(request.getUnit());
        if (request.getPrice() != null) ingredient.setPrice(request.getPrice());
        if (request.getExpiryDate() != null) ingredient.setExpiryDate(request.getExpiryDate());
        if (request.getLowStockThreshold() != null) ingredient.setLowStockThreshold(request.getLowStockThreshold());
    }

    @Transactional
    public void deleteIngredient(Long retailerId, Long id) {
        Ingredient ingredient = verifyIngredientAccess(retailerId, id);
        ingredient.setActive(false);
        ingredientRepository.save(ingredient);
    }

    @Transactional
    public IngredientDTO restoreIngredient(Long retailerId, Long id) {
        Ingredient ingredient = verifyIngredientAccess(retailerId, id);
        ingredient.setActive(true);
        return convertToDTO(ingredientRepository.save(ingredient));
    }

    private Ingredient verifyIngredientAccess(Long retailerId, Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        if (!ingredient.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized access to ingredient");
        }

        return ingredient;
    }

    @Transactional(readOnly = true)
    public String exportInventory(Long retailerId) {
        try {
            List<Ingredient> ingredients = ingredientRepository.findByRetailerIdAndActiveTrue(retailerId);
            List<IngredientCsvDTO> csvDtos = mapToCsvDTOs(ingredients);

            StringWriter writer = new StringWriter();
            StatefulBeanToCsv<IngredientCsvDTO> beanToCsv = new StatefulBeanToCsvBuilder<IngredientCsvDTO>(writer)
                    .withQuotechar('"')
                    .withSeparator(',')
                    .build();

            beanToCsv.write(csvDtos);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export inventory: " + e.getMessage());
        }
    }

    private List<IngredientCsvDTO> mapToCsvDTOs(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> {
                    IngredientCsvDTO dto = new IngredientCsvDTO();
                    dto.setName(ingredient.getName());
                    dto.setCategory(ingredient.getCategory().name());
                    dto.setQuantity(ingredient.getQuantity());
                    dto.setUnit(ingredient.getUnit());
                    dto.setPrice(ingredient.getPrice());
                    dto.setExpiryDate(ingredient.getExpiryDate());
                    dto.setLowStockThreshold(ingredient.getLowStockThreshold());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> importInventory(Long retailerId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Please select a file to import");
        }

        try {
            User retailer = userRepository.findById(retailerId)
                    .orElseThrow(() -> new RuntimeException("Retailer not found"));

            List<Ingredient> importedIngredients = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            processCSVFile(file, retailer, importedIngredients, errors);

            if (!errors.isEmpty()) {
                throw new RuntimeException(String.join(", ", errors));
            }

            ingredientRepository.saveAll(importedIngredients);
            return Map.of(
                    "message", "Successfully imported " + importedIngredients.size() + " ingredients",
                    "count", importedIngredients.size()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to import inventory: " + e.getMessage());
        }
    }

    private void processCSVFile(MultipartFile file, User retailer, List<Ingredient> importedIngredients, List<String> errors) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<IngredientCsvDTO> csvToBean = new CsvToBeanBuilder<IngredientCsvDTO>(reader)
                    .withType(IngredientCsvDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            int rowNumber = 1;
            for (IngredientCsvDTO csvDto : csvToBean) {
                rowNumber++;
                try {
                    importedIngredients.add(createIngredientFromCsvDTO(csvDto, retailer));
                } catch (Exception e) {
                    errors.add("Error in row " + rowNumber + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage());
        }
    }

    private Ingredient createIngredientFromCsvDTO(IngredientCsvDTO csvDto, User retailer) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(csvDto.getName());
        ingredient.setCategory(IngredientCategory.valueOf(csvDto.getCategory().toUpperCase()));
        ingredient.setQuantity(csvDto.getQuantity());
        ingredient.setUnit(csvDto.getUnit());
        ingredient.setPrice(csvDto.getPrice());
        ingredient.setExpiryDate(csvDto.getExpiryDate());
        ingredient.setLowStockThreshold(csvDto.getLowStockThreshold());
        ingredient.setRetailer(retailer);
        ingredient.setActive(true);
        return ingredient;
    }

    @Transactional(readOnly = true)
    public List<IngredientDTO> getDeletedIngredients(Long retailerId, String search, IngredientCategory category) {
        List<Ingredient> deletedIngredients;

        if (search != null && !search.trim().isEmpty()) {
            deletedIngredients = ingredientRepository.searchDeletedIngredients(retailerId, search.trim());
        } else if (category != null) {
            deletedIngredients = ingredientRepository.findByRetailerIdAndCategoryAndActiveFalse(retailerId, category);
        } else {
            deletedIngredients = ingredientRepository.findByRetailerIdAndActiveFalse(retailerId);
        }

        return convertToDTOList(deletedIngredients);
    }
}