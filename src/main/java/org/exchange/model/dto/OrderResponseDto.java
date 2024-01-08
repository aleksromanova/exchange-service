package org.exchange.model.dto;

import org.exchange.model.entity.OrderStatus;
import org.exchange.model.entity.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponseDto(
        Long id,
        Long userId,
        String asset,
        BigDecimal price,
        BigDecimal fee,
        OrderType type,
        OrderStatus status,
        Instant timestamp) {

}
