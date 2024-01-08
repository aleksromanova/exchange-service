package org.exchange.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record ErrorResponseDto(@Schema(example = "error message")String message) {
}
