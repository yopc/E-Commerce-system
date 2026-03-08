package com.example.Spring_traning.service;

import com.example.Spring_traning.dto.AddToCartDto;
import com.example.Spring_traning.dto.CartDto;
import com.example.Spring_traning.dto.CartItemDto;
import com.example.Spring_traning.entity.Cart;
import com.example.Spring_traning.entity.CartItem;
import com.example.Spring_traning.entity.Product;
import com.example.Spring_traning.entity.User;
import com.example.Spring_traning.exception.ResourceNotFoundException;
import com.example.Spring_traning.repository.CartItemRepository;
import com.example.Spring_traning.repository.CartRepository;
import com.example.Spring_traning.repository.ProductRepository;
import com.example.Spring_traning.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartDto addToCart(AddToCartDto addToCartDto) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + addToCartDto.getProductId()));

        // Check if product already in cart
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(addToCartDto.getQuantity());
            cart.getItems().add(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + addToCartDto.getQuantity());
        }

        cartItemRepository.save(cartItem);
        return getCartDto(cart);
    }

    @Transactional
    public CartDto updateCartItem(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCartDto(cartItem.getCart());
    }

    @Transactional
    public CartDto removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        return getCartDto(cart);
    }

    public CartDto getCurrentUserCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        return getCartDto(cart);
    }

    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartDto getCartDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setUserId(cart.getUser().getId());
        cartDto.setTotalPrice(cart.getTotalPrice());

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        cartDto.setItems(itemDtos);

        return cartDto;
    }

    private CartItemDto mapToDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setUnitPrice(cartItem.getProduct().getPrice());
        dto.setSubtotal(cartItem.getSubtotal());
        dto.setImageUrl(cartItem.getProduct().getImageUrl());
        return dto;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
