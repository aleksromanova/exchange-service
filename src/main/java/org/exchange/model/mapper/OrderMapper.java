package org.exchange.model.mapper;

import org.exchange.model.dto.OrderResponseDto;
import org.exchange.model.entity.OrderEntity;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponseDto mapOrderEntityToResponseDto(OrderEntity orderEntity) {
        return new OrderResponseDto(orderEntity.getId(), orderEntity.getUser().getId(), orderEntity.getAsset().getShortName(), orderEntity.getPrice(), orderEntity.getFee(), orderEntity.getType(), orderEntity.getStatus(), orderEntity.getTimestamp());
    }
}
