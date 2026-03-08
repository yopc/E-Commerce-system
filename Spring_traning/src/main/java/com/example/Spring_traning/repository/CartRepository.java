package com.example.Spring_traning.repository;

import com.example.Spring_traning.entity.Cart;
import com.example.Spring_traning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserId(Long userId);
}
