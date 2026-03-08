package com.example.Spring_traning.service;

import com.example.Spring_traning.dto.OrderDto;
import com.example.Spring_traning.entity.*;
import com.example.Spring_traning.repository.CartRepository;
import com.example.Spring_traning.repository.OrderRepository;
import com.example.Spring_traning.repository.ProductRepository;
import com.example.Spring_traning.repository.UserRepository;
import com.example.Spring_traning.dto.OrderItemDto;

import com.example.Spring_traning.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Transactional
    public OrderDto placeOrder() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        // Create order
        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(cart.getTotalPrice());

        // Create order items and update stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Check stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            order.getOrderItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cartService.clearCart();

        return mapToDto(savedOrder);
    }

    public Page<OrderDto> getUserOrders(Pageable pageable) {
        User currentUser = getCurrentUser();
        return orderRepository.findByUser(currentUser, pageable).map(this::mapToDto);
    }

    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToDto(order);
    }

    public OrderDto getOrderByOrderNumber(String orderNumber) {
        // For simplicity, not implementing this, but you could add a method in repository
        throw new UnsupportedOperationException("Not implemented");
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDto);
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        dto.setOrderItems(itemDtos);

        return dto;
    }

    private OrderItemDto mapToDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setImageUrl(orderItem.getProduct().getImageUrl());
        return dto;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
