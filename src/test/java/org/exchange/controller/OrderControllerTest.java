package org.exchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.exchange.exception.AssetNotRecognizedException;
import org.exchange.exception.OrderCancellationException;
import org.exchange.exception.OrderNotFoundException;
import org.exchange.exception.UserNotFoundException;
import org.exchange.model.dto.OrderRequestDto;
import org.exchange.model.dto.OrderResponseDto;
import org.exchange.model.entity.OrderStatus;
import org.exchange.model.entity.OrderType;
import org.exchange.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrderTest() throws Exception {
        Long orderId = 1L;
        Mockito.when(orderService.getOrderById(orderId)).thenReturn(new OrderResponseDto(1L, 2L, "BTC", new BigDecimal(200), new BigDecimal(35), OrderType.BUY, OrderStatus.NEW, Instant.now()));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void getOrderNotFoundTest() throws Exception {
        var orderId = 1L;
        Mockito.when(orderService.getOrderById(orderId)).thenThrow(new OrderNotFoundException());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchOrdersTest() throws Exception {
        var userId = 1L;
        var orders = List.of(
                new OrderResponseDto(1L, 1L, "BTC", new BigDecimal(200), new BigDecimal(35), OrderType.BUY, OrderStatus.NEW, Instant.now()),
                new OrderResponseDto(2L, 1L, "ETH", new BigDecimal(300), new BigDecimal(5), OrderType.SELL, OrderStatus.NEW, Instant.now()));
        Mockito.when(orderService.searchOrders(eq(userId), isNull(), any(Pageable.class))).thenReturn(orders);

        mockMvc.perform(get("/api/v1/orders")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(orders.size())));
    }

    @Test
    void searchOrdersUserNotFoundTest() throws Exception {
        var userId = 1L;
        Mockito.when(orderService.searchOrders(eq(userId), any(OrderStatus.class), any(Pageable.class))).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/api/v1/orders")
                        .param("userId", String.valueOf(userId))
                        .param("status", String.valueOf(OrderStatus.NEW)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderTest() throws Exception {
        var orderRequest = new OrderRequestDto(1L, "BTC",  new BigDecimal(200), OrderType.BUY);
        var orderResponse = new OrderResponseDto(1L, 1L, "BTC", new BigDecimal(200), new BigDecimal(35), OrderType.BUY, OrderStatus.NEW, Instant.now());
        Mockito.when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void createOrderWrongAssetTest() throws Exception {
        var orderRequest = new OrderRequestDto(1L, "ABC",  new BigDecimal(200), OrderType.BUY);
        Mockito.when(orderService.createOrder(any(OrderRequestDto.class))).thenThrow(new AssetNotRecognizedException());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderWrongPriceTest() throws Exception {
        var orderRequest = new OrderRequestDto(1L, "BTC",  new BigDecimal(-200), OrderType.BUY);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderWrongUserIdTest() throws Exception {
        var orderRequest = new OrderRequestDto(1L, "BTC",  new BigDecimal(-200), OrderType.BUY);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrderTest() throws Exception {
        var orderId = 1L;
        Mockito.doNothing().when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk());
        Mockito.verify(orderService).cancelOrder(orderId);
    }

    @Test
    void cancelOrderNotFoundTest() throws Exception {
        var orderId = 1L;
        Mockito.doThrow(new OrderNotFoundException()).when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrderAlreadyCancelledTest() throws Exception {
        var orderId = 1L;
        Mockito.doThrow(new OrderCancellationException()).when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isBadRequest());
    }
}
