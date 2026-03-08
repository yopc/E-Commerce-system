package com.example.Spring_traning.repository;

import com.example.Spring_traning.entity.Order;
import com.example.Spring_traning.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
