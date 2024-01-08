package org.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.exchange.exception.AssetNotRecognizedException;
import org.exchange.exception.OrderCancellationException;
import org.exchange.exception.OrderNotFoundException;
import org.exchange.exception.UserNotFoundException;
import org.exchange.model.dto.ErrorResponseDto;
import org.exchange.model.dto.OrderRequestDto;
import org.exchange.model.dto.OrderResponseDto;
import org.exchange.model.entity.OrderStatus;
import org.exchange.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get order by Id")
    @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = OrderResponseDto.class), mediaType = "application/json")})
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema())})
    @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema())})
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable("id") @Parameter(example = "1") Long orderId) throws OrderNotFoundException {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Operation(summary = "Search orders")
    @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = OrderResponseDto.class), mediaType = "application/json")})
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> searchOrders(@RequestParam @Parameter(example = "1") Long userId,
                                                               @RequestParam(required = false) @Parameter(example = "NEW") OrderStatus status,
                                                               @Parameter(example = """
                                                                       {
                                                                         "page": 0,
                                                                         "size": 5,
                                                                         "sort": [
                                                                           "timestamp,asc"
                                                                         ]
                                                                       }""") Pageable pageable) throws UserNotFoundException {
        return ResponseEntity.ok(orderService.searchOrders(userId, status, pageable));
    }

    @Operation(summary = "Create order")
    @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(implementation = OrderResponseDto.class))})
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Validated @RequestBody OrderRequestDto orderDto) throws UserNotFoundException, AssetNotRecognizedException {
        var response = orderService.createOrder(orderDto);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Cancel order")
    @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = OrderResponseDto.class))})
    @ApiResponse(responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @ApiResponse(responseCode = "404", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @ApiResponse(responseCode = "500", content = {@Content(schema = @Schema(implementation = ErrorResponseDto.class))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable("id") @Parameter(example = "1") Long orderId) throws OrderNotFoundException, OrderCancellationException {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
