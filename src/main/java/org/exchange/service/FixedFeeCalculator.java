package org.exchange.service;

import org.exchange.model.entity.OrderEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FixedFeeCalculator implements FeeCalculator {

    @Override
    public BigDecimal calculateFee(OrderEntity order) {
        return order.getUser().getFee().multiply(order.getPrice());
    }
}
