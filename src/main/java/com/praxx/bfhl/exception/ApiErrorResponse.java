package com.praxx.bfhl.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        @JsonProperty("is_success")
        boolean success,
        String message,
        List<String> errors,
        Instant timestamp
) {
}
