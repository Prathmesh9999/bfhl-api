package com.praxx.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AsyncBfhlResponse(
        @JsonProperty("is_success")
        boolean success,
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("correlation_id")
        String correlationId,
        String status,
        @JsonProperty("processing_time_ms")
        long processingTimeMs
) implements BfhlResponseContract {
}
