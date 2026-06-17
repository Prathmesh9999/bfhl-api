package com.praxx.bfhl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BfhlResponse(
        @JsonProperty("is_success")
        boolean success,
        @JsonProperty("request_id")
        String requestId,
        @JsonProperty("odd_numbers")
        List<String> oddNumbers,
        @JsonProperty("even_numbers")
        List<String> evenNumbers,
        List<String> alphabets,
        @JsonProperty("special_characters")
        List<String> specialCharacters,
        String sum,
        @JsonProperty("largest_number")
        String largestNumber,
        @JsonProperty("smallest_number")
        String smallestNumber,
        @JsonProperty("alphabet_count")
        int alphabetCount,
        @JsonProperty("number_count")
        int numberCount,
        @JsonProperty("special_character_count")
        int specialCharacterCount,
        @JsonProperty("contains_duplicates")
        boolean containsDuplicates,
        @JsonProperty("processing_time_ms")
        long processingTimeMs
) implements BfhlResponseContract {
}
