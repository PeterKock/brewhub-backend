package nl.pkock.brewhub_backend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.pkock.brewhub_backend.order.dto.*;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.createOrder(userPrincipal.getId(), request));
    }

    @GetMapping("/user/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getUserOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getUserOrders(userPrincipal.getId()));
    }

    @GetMapping("/retailer/orders")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<List<OrderDTO>> getRetailerOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getRetailerOrders(userPrincipal.getId()));
    }

    @PutMapping("/retailer/orders/{orderId}/status")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.updateOrderStatus(userPrincipal.getId(), orderId, request));
    }

    @GetMapping("/user/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDTO> getUserOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getOrder(orderId, userPrincipal.getId(), false));
    }

    @GetMapping("/retailer/orders/{orderId}")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<OrderDTO> getRetailerOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getOrder(orderId, userPrincipal.getId(), true));
    }

    @DeleteMapping("/user/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        orderService.cancelOrder(userPrincipal.getId(), orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/retailer/dashboard/stats")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<?> getRetailerDashboardStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getRetailerDashboardStats(
                userPrincipal.getId(),
                userPrincipal.getUsername()));
    }

    @GetMapping("/retailer/dashboard/recent-orders")
    @PreAuthorize("hasRole('RETAILER')")
    public ResponseEntity<List<OrderDTO>> getRetailerRecentOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getRecentOrders(userPrincipal.getId(), true));
    }

    @GetMapping("/user/dashboard/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserDashboardStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getUserDashboardStats(userPrincipal.getId()));
    }

    @GetMapping("/user/dashboard/recent-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDTO>> getUserRecentOrders(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getRecentOrders(userPrincipal.getId(), false));
    }
}