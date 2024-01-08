package org.exchange.service;

import org.exchange.model.entity.AssetEntity;
import org.exchange.model.entity.OrderEntity;
import org.exchange.model.entity.UserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class FixedFeeCalculatorTest {
    private final FeeCalculator feeCalculator = new FixedFeeCalculator();

    @Test
    void calculateFeeTest() {
        var userFee = new BigDecimal("0.05");
        var price = new BigDecimal(200);
        var userEntity = UserEntity.builder().id(1L).fee(userFee).build();
        var orderEntity = OrderEntity.builder()
                .id(1L)
                .price(price)
                .user(userEntity)
                .asset(AssetEntity.builder().id(1L).shortName("BTC").build())
                .build();

        var fee = feeCalculator.calculateFee(orderEntity);

        Assertions.assertEquals(userFee.multiply(price), fee);
    }
}
