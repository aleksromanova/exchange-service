package org.exchange.service;

import org.exchange.model.entity.OrderEntity;

import java.math.BigDecimal;

public interface FeeCalculator {
    BigDecimal calculateFee(OrderEntity order);
}
