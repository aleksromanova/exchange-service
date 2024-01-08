package org.exchange.service;

import org.exchange.exception.AssetNotRecognizedException;
import org.exchange.exception.OrderCancellationException;
import org.exchange.exception.UserNotFoundException;
import org.exchange.exception.OrderNotFoundException;
import org.exchange.model.dto.OrderRequestDto;
import org.exchange.model.dto.OrderResponseDto;
import org.exchange.model.entity.OrderEntity;
import org.exchange.model.entity.OrderStatus;
import org.exchange.model.mapper.OrderMapper;
import org.exchange.repository.AssetRepository;
import org.exchange.repository.OrderRepository;
import org.exchange.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FeeCalculator feeCalculator;

    private final AssetRepository assetRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, AssetRepository assetRepository, FeeCalculator feeCalculator) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.feeCalculator = feeCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) throws OrderNotFoundException {
        var orderEntity = orderRepository.findById(orderId)
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .orElseThrow(() -> new OrderNotFoundException("Order is not found"));
        return OrderMapper.mapOrderEntityToResponseDto(orderEntity);
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderDto) throws UserNotFoundException, AssetNotRecognizedException {
        var userEntity = userRepository.findById(orderDto.userId()).orElseThrow(() -> new UserNotFoundException("User is not found"));
        var asset = assetRepository.findByShortName(orderDto.asset()).orElseThrow(() -> new AssetNotRecognizedException("Asset does not exist"));
        OrderEntity orderEntity = OrderEntity.builder().asset(asset)
                .user(userEntity)
                .status(OrderStatus.NEW)
                .price(orderDto.price())
                .type(orderDto.type())
                .timestamp(Instant.now())
                .build();
        orderEntity.setFee(feeCalculator.calculateFee(orderEntity));
        return OrderMapper.mapOrderEntityToResponseDto(orderRepository.save(orderEntity));
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) throws OrderNotFoundException, OrderCancellationException {
        var orderEntity = orderRepository.findById(orderId)
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .orElseThrow(() -> new OrderNotFoundException("Order is not found"));
        if(orderEntity.getStatus() == OrderStatus.COMPLETED) {
            throw new OrderCancellationException("Order is already completed and can not be canceled");
        }
        orderEntity.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(orderEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> searchOrders(Long userId, OrderStatus status, Pageable pageable) throws UserNotFoundException {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User doesn't exists"));
        Page<OrderEntity> userOrders;
        if(status == null) {
            userOrders = orderRepository.findByUserIdAndStatusNot(userId, OrderStatus.CANCELLED, pageable);
        } else {
            userOrders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return userOrders
                .map(OrderMapper::mapOrderEntityToResponseDto)
                .toList();
    }
}
