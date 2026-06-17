package com.praxx.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Summary(
        @JsonProperty("total_elements_received")
        int totalElementsReceived,
        @JsonProperty("valid_elements_processed")
        int validElementsProcessed,
        @JsonProperty("invalid_elements_ignored")
        int invalidElementsIgnored
) {
}
