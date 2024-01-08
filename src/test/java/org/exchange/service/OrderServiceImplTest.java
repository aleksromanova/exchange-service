package org.exchange.service;

import org.exchange.exception.OrderCancellationException;
import org.exchange.exception.OrderNotFoundException;
import org.exchange.exception.UserNotFoundException;
import org.exchange.model.dto.OrderRequestDto;
import org.exchange.model.entity.AssetEntity;
import org.exchange.model.entity.OrderEntity;
import org.exchange.model.entity.OrderStatus;
import org.exchange.model.entity.OrderType;
import org.exchange.model.entity.UserEntity;
import org.exchange.repository.AssetRepository;
import org.exchange.repository.OrderRepository;
import org.exchange.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {
    private OrderService service;

    private OrderRepository orderRepository;

    private UserRepository userRepository;

    private AssetRepository assetRepository;

    private FeeCalculator feeCalculator;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        assetRepository = Mockito.mock(AssetRepository.class);
        feeCalculator = Mockito.mock(FeeCalculator.class);
        service = new OrderServiceImpl(orderRepository, userRepository, assetRepository, feeCalculator);
    }

    @Test
    void getOrderByIdTest() throws Exception {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName("BTC").name("Bitcoin").build())
                .status(OrderStatus.NEW)
                .price(new BigDecimal(100))
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        var orderDto = service.getOrderById(orderId);

        Assertions.assertEquals(orderId, orderDto.id());
        Assertions.assertEquals(orderEntity.getUser().getId(), orderDto.userId());
        Assertions.assertEquals(orderEntity.getAsset().getId(), assetId);
        Assertions.assertEquals(orderEntity.getPrice(), orderDto.price());
        Assertions.assertEquals(orderEntity.getStatus(), orderDto.status());
    }

    @Test
    void getOrderByIdNotFoundTest() {
        Assertions.assertThrows(OrderNotFoundException.class, () -> service.getOrderById(1L));
    }

    @Test
    void getOrderByIdCancelledTest() {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName("BTC").name("Bitcoin").build())
                .status(OrderStatus.CANCELLED)
                .price(new BigDecimal(100))
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        Assertions.assertThrows(OrderNotFoundException.class, () -> service.getOrderById(1L));
    }

    @Test
    void searchOrdersTest() throws Exception {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName("BTC").name("Bitcoin").build())
                .status(OrderStatus.NEW)
                .price(new BigDecimal(100))
                .build();
        var pageable = PageRequest.of(0, 1).withSort(Sort.by("timestamp").ascending());
        when(orderRepository.findByUserIdAndStatusNot(userId, OrderStatus.CANCELLED, pageable))
                .thenReturn(new PageImpl<>(List.of(orderEntity)));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(UserEntity.builder().id(userId).build()));

        var orders = service.searchOrders(userId, null, pageable);

        Assertions.assertEquals(1, orders.size());
    }

    @Test
    void searchOrdersWithStatusTest() throws Exception {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var status = OrderStatus.NEW;
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName("BTC").name("Bitcoin").build())
                .status(status)
                .price(new BigDecimal(100))
                .build();
        var pageable = PageRequest.of(0, 1).withSort(Sort.by("timestamp").ascending());
        when(orderRepository.findByUserIdAndStatus(userId, status, pageable))
                .thenReturn(new PageImpl<>(List.of(orderEntity)));
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(UserEntity.builder().id(userId).build()));

        var orders = service.searchOrders(userId, status, pageable);

        Assertions.assertEquals(1, orders.size());
    }

    @Test
    void searchOrdersUserNotFoundTest() {
        var pageable = PageRequest.of(0, 1).withSort(Sort.by("timestamp").ascending());
        Assertions.assertThrows(UserNotFoundException.class, () -> service.searchOrders(1L, null, pageable));
    }

    @Test
    void createOrderTest() throws Exception {
        var userId = 2L;
        var assetId = 3L;
        var assetShortName = "BTC";
        var fee = new BigDecimal("3.25");
        var orderEntity = OrderEntity.builder().id(1L)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName(assetShortName).name("Bitcoin").build())
                .fee(fee)
                .status(OrderStatus.NEW)
                .price(new BigDecimal(100))
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(UserEntity.builder().id(userId).fee(new BigDecimal("0.05")).build()));
        when(assetRepository.findByShortName(assetShortName)).thenReturn(Optional.of(AssetEntity.builder().id(assetId).shortName(assetShortName).build()));
        when(feeCalculator.calculateFee(any(OrderEntity.class))).thenReturn(fee);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        var request = new OrderRequestDto(userId, assetShortName, new BigDecimal(100), OrderType.BUY);

        var orderDto = service.createOrder(request);

        Mockito.verify(orderRepository).save(any(OrderEntity.class));
        Assertions.assertEquals(fee, orderDto.fee());
    }

    @Test
    void createOrderUserNotFoundTest() {
        var request = new OrderRequestDto(2L, "BTC", new BigDecimal(100), OrderType.BUY);
        Assertions.assertThrows(UserNotFoundException.class, () -> service.createOrder(request));
    }

    @Test
    void cancelOrderTest() throws Exception {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var assetShortName = "BTC";
        var fee = new BigDecimal("3.25");
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName(assetShortName).name("Bitcoin").build())
                .fee(fee)
                .status(OrderStatus.NEW)
                .price(new BigDecimal(100))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        service.cancelOrder(orderId);

        Assertions.assertEquals(OrderStatus.CANCELLED, orderEntity.getStatus());
        Mockito.verify(orderRepository).save(orderEntity);
    }

    @Test
    void cancelOrderNotFoundTest() {
        Assertions.assertThrows(OrderNotFoundException.class, () -> service.cancelOrder(1L));
    }

    @Test
    void cancelOrderAlreadyCancelledTest() {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var assetShortName = "BTC";
        var fee = new BigDecimal("3.25");
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName(assetShortName).name("Bitcoin").build())
                .fee(fee)
                .status(OrderStatus.CANCELLED)
                .price(new BigDecimal(100))
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Assertions.assertThrows(OrderNotFoundException.class, () -> service.cancelOrder(1L));
    }

    @Test
    void cancelOrderAlreadyCompletedTest() {
        var orderId = 1L;
        var userId = 2L;
        var assetId = 3L;
        var assetShortName = "BTC";
        var fee = new BigDecimal("3.25");
        var orderEntity = OrderEntity.builder().id(orderId)
                .user(UserEntity.builder().id(userId).build())
                .asset(AssetEntity.builder().id(assetId).shortName(assetShortName).name("Bitcoin").build())
                .fee(fee)
                .status(OrderStatus.COMPLETED)
                .price(new BigDecimal(100))
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        Assertions.assertThrows(OrderCancellationException.class, () -> service.cancelOrder(1L));
    }
}
