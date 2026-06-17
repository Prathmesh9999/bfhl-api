package com.praxx.bfhl.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BfhlRequest(
        @NotNull(message = "data is required")
        @Size(max = 100000, message = "data can contain at most 100000 elements")
        List<Object> data
) {
}
