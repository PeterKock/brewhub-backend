package nl.pkock.brewhub_backend.order.service;

import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.order.dto.*;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.inventory.models.Ingredient;
import nl.pkock.brewhub_backend.order.models.Order;
import nl.pkock.brewhub_backend.order.models.OrderItem;
import nl.pkock.brewhub_backend.order.models.OrderStatus;
import nl.pkock.brewhub_backend.order.repository.OrderRepository;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.inventory.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

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
        dto.setItems(convertOrderItems(order.getItems()));
        dto.setRetailerRating(order.getRetailer().getAverageRating());
        dto.setRetailerTotalRatings(order.getRetailer().getTotalRatings());
        return dto;
    }

    private List<OrderItemDTO> convertOrderItems(List<OrderItem> items) {
        return items.stream()
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
    }

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User retailer = userRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRetailer(retailer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.getNotes());

        List<OrderItem> orderItems = createOrderItems(order, request.getItems());
        order.setItems(orderItems);
        order.setTotalPrice(calculateTotalPrice(orderItems));

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    private List<OrderItem> createOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
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

            ingredient.setQuantity(ingredient.getQuantity().subtract(itemRequest.getQuantity()));
            ingredientRepository.save(ingredient);
        }

        return orderItems;
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByCustomerId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getRetailerOrders(Long retailerId) {
        return orderRepository.findByRetailerId(retailerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long retailerId, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized to update this order");
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId, Long userId, boolean isRetailer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (isRetailer && !order.getRetailer().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this order");
        } else if (!isRetailer && !order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return convertToDTO(order);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending orders");
        }

        returnItemsToInventory(order.getItems());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void returnItemsToInventory(List<OrderItem> items) {
        for (OrderItem item : items) {
            Ingredient ingredient = item.getIngredient();
            ingredient.setQuantity(ingredient.getQuantity().add(item.getQuantity()));
            ingredientRepository.save(ingredient);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRetailerDashboardStats(Long retailerId, String retailerName) {
        List<Order> allOrders = orderRepository.findByRetailerId(retailerId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("name", retailerName);
        stats.put("pendingOrders", countOrdersByStatus(allOrders, OrderStatus.PENDING));
        stats.put("completedOrders", countOrdersByStatus(allOrders, OrderStatus.DELIVERED));
        stats.put("shippedOrders", countOrdersByStatus(allOrders, OrderStatus.SHIPPED));

        return stats;
    }

    private long countOrdersByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream()
                .filter(o -> o.getStatus() == status)
                .count();
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getRecentOrders(Long userId, boolean isRetailer) {
        List<Order> allOrders = isRetailer ?
                orderRepository.findByRetailerId(userId) :
                orderRepository.findByCustomerId(userId);

        return allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserDashboardStats(Long userId) {
        List<Order> allOrders = orderRepository.findByCustomerId(userId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalOrders", allOrders.size());
        stats.put("favoriteRetailers", countUniqueRetailers(allOrders));

        return stats;
    }

    private long countUniqueRetailers(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getRetailer().getId())
                .distinct()
                .count();
    }
}