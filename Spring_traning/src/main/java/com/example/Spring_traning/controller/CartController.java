package com.example.Spring_traning.controller;

import com.example.Spring_traning.dto.AddToCartDto;
import com.example.Spring_traning.dto.CartDto;
import com.example.Spring_traning.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCurrentUserCart() {
        CartDto cart = cartService.getCurrentUserCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@Valid @RequestBody AddToCartDto addToCartDto) {
        CartDto updatedCart = cartService.addToCart(addToCartDto);
        return new ResponseEntity<>(updatedCart, HttpStatus.CREATED);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartDto updatedCart = cartService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long cartItemId) {
        CartDto updatedCart = cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
