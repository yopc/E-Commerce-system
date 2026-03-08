package com.example.Spring_traning.repository;

import com.example.Spring_traning.entity.CartItem;
import com.example.Spring_traning.entity.Product;
import com.example.Spring_traning.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartId(Long cartId);
}
