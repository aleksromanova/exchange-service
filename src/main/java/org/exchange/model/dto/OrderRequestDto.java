package org.exchange.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.exchange.model.entity.OrderType;

import java.math.BigDecimal;

@Schema
public record OrderRequestDto(@NotNull(message = "UserId is mandatory")
                              @Schema(example = "1")
                              Long userId,
                              @NotBlank(message = "Asset is mandatory")
                              @Schema(example = "BTC")
                              String asset,
                              @NotNull(message = "Price is mandatory")
                              @DecimalMin(value = "0.0", inclusive = false, message = "Price should be greater than 0")
                              @Digits(integer = 5, fraction = 2)
                              @Schema(example = "100.50")
                              BigDecimal price,
                              @NotNull(message = "Type is mandatory")
                              @Schema(example = "BUY")
                              OrderType type) {

}
