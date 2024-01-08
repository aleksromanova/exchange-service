package org.exchange.service;

import org.exchange.exception.AssetNotRecognizedException;
import org.exchange.exception.OrderCancellationException;
import org.exchange.exception.OrderNotFoundException;
import org.exchange.exception.UserNotFoundException;
import org.exchange.model.dto.OrderRequestDto;
import org.exchange.model.dto.OrderResponseDto;
import org.exchange.model.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponseDto getOrderById(Long orderId) throws OrderNotFoundException;

    List<OrderResponseDto> searchOrders(Long userId, OrderStatus status, Pageable pageable) throws UserNotFoundException;

    OrderResponseDto createOrder(OrderRequestDto order) throws UserNotFoundException, AssetNotRecognizedException;

    void cancelOrder(Long orderId) throws OrderNotFoundException, OrderCancellationException;
}
