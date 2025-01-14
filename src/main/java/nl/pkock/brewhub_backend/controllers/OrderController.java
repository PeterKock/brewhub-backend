package nl.pkock.brewhub_backend.controllers;

import jakarta.validation.Valid;
import nl.pkock.brewhub_backend.dto.*;
import nl.pkock.brewhub_backend.models.*;
import nl.pkock.brewhub_backend.repositories.OrderRepository;
import nl.pkock.brewhub_backend.repositories.UserRepository;
import nl.pkock.brewhub_backend.repositories.IngredientRepository;
import nl.pkock.brewhub_backend.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public OrderController(OrderRepository orderRepository,
                           UserRepository userRepository,
                           IngredientRepository ingredientRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.ingredientRepository = ingredientRepository;
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setRetailerId(order.getRetailer().getId());
        dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        dto.setRetailerName(order.getRetailer().getFirstName() + " " + order.getRetailer().getLastName());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setNotes(order.getNotes());

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setIngredientId(item.getIngredient().getId());
                    itemDTO.setIngredientName(item.getIngredient().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setUnit(item.getIngredient().getUnit());
                    itemDTO.setPricePerUnit(item.getPricePerUnit());
                    itemDTO.setTotalPrice(item.getTotalPrice());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        dto.setRetailerRating(order.getRetailer().getAverageRating());
        dto.setRetailerTotalRatings(order.getRetailer().getTotalRatings());
        return dto;
    }

    @PostMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User customer = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User retailer = userRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRetailer(retailer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.getNotes());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemRequest.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found"));

            if (ingredient.getQuantity().compareTo(itemRequest.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient stock for " + ingredient.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setIngredient(ingredient);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPricePerUnit(ingredient.getPrice());
            orderItem.setTotalPrice(ingredient.getPrice().multiply(itemRequest.getQuantity()));

            orderItems.add(orderItem);
            totalPrice = totalPrice.add(orderItem.getTotalPrice());

            // Update ingredient quantity
            ingredient.setQuantity(ingredient.getQuantity().subtract(itemRequest.getQuantity()));
            ingredientRepository.save(ingredient);
        }

        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(convertToDTO(savedOrder));
    }

    @GetMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getUserOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Order> orders = orderRepository.findByCustomerId(userPrincipal.getId());
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/retailer/orders")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<List<OrderDTO>> getRetailerOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Order> orders = orderRepository.findByRetailerId(userPrincipal.getId());
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @PutMapping("/retailer/orders/{orderId}/status")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRetailer().getId().equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to update this order");
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);
        return ResponseEntity.ok(convertToDTO(updatedOrder));
    }

    @GetMapping("/user/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> getUserOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return ResponseEntity.ok(convertToDTO(order));
    }

    @GetMapping("/retailer/orders/{orderId}")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<OrderDTO> getRetailerOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRetailer().getId().equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return ResponseEntity.ok(convertToDTO(order));
    }

    @DeleteMapping("/user/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending orders");
        }

        // Return quantities to inventory
        for (OrderItem item : order.getItems()) {
            Ingredient ingredient = item.getIngredient();
            ingredient.setQuantity(ingredient.getQuantity().add(item.getQuantity()));
            ingredientRepository.save(ingredient);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/retailer/dashboard/stats")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<?> getRetailerDashboardStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long retailerId = userPrincipal.getId();

        List<Order> allOrders = orderRepository.findByRetailerId(retailerId);

        // Calculate stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("name", userPrincipal.getUsername());
        stats.put("pendingOrders", allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count());
        stats.put("completedOrders", allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count());
        stats.put("totalProducts", ingredientRepository.findByRetailerId(retailerId).size());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/retailer/dashboard/recent-orders")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<List<OrderDTO>> getRetailerRecentOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        List<Order> allOrders = orderRepository.findByRetailerId(userPrincipal.getId());

        // Get 5 most recent orders
        List<Order> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .toList();

        return ResponseEntity.ok(recentOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/user/dashboard/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserDashboardStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Order> allOrders = orderRepository.findByCustomerId(userPrincipal.getId());

        // Calculate stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", allOrders.size());

        // Count unique retailers
        long favoriteRetailers = allOrders.stream()
                .map(order -> order.getRetailer().getId())
                .distinct()
                .count();

        stats.put("favoriteRetailers", favoriteRetailers);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/dashboard/recent-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getUserRecentOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        List<Order> allOrders = orderRepository.findByCustomerId(userPrincipal.getId());

        // Get 5 most recent orders
        List<Order> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .toList();

        return ResponseEntity.ok(recentOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }
}