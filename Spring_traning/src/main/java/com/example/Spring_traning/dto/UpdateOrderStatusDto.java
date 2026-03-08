package com.example.Spring_traning.dto;

import com.example.Spring_traning.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusDto {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}
