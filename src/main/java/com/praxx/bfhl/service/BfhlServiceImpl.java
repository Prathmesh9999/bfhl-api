package com.praxx.bfhl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.praxx.bfhl.config.ApiProperties;
import com.praxx.bfhl.dto.AsyncBfhlResponse;
import com.praxx.bfhl.dto.BfhlRequest;
import com.praxx.bfhl.dto.BfhlResponse;
import com.praxx.bfhl.dto.BfhlResponseContract;

@Service
public class BfhlServiceImpl implements BfhlService {

    private static final Logger log = LoggerFactory.getLogger(BfhlServiceImpl.class);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:\\.\\d+)?");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("[A-Za-z]+");

    private final ApiProperties apiProperties;

    public BfhlServiceImpl(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Override
    public BfhlResponseContract process(BfhlRequest request, String requestId) {
        long started = System.nanoTime();
        if (request.data().size() > apiProperties.asyncThreshold()) {
            String correlationId = UUID.randomUUID().toString();
            CompletableFuture.runAsync(() -> processLargePayloadAsync(request, requestId, correlationId));
            return new AsyncBfhlResponse(true, requestId, correlationId, "ACCEPTED", elapsedMillis(started));
        }
        return processSynchronously(request, requestId, started);
    }

    BfhlResponse processSynchronously(BfhlRequest request, String requestId, long started) {
        DeduplicationResult deduplicationResult = deduplicate(request.data());
        ProcessingState state = new ProcessingState();
        for (String value : deduplicationResult.uniqueValues()) {
            processValue(value, state);
        }

        state.numericValues.sort(Comparator.naturalOrder());
        BigDecimal sum = state.numericValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        String smallest = state.numericValues.isEmpty() ? "" : formatNumber(state.numericValues.get(0));
        String largest = state.numericValues.isEmpty() ? "" : formatNumber(state.numericValues.get(state.numericValues.size() - 1));

        BfhlResponse response = new BfhlResponse(
                true,
                requestId,
                state.oddNumbers,
                state.evenNumbers,
                state.alphabets,
                state.specialCharacters,
                formatNumber(sum),
                largest,
                smallest,
                state.alphabets.size(),
                state.numericValues.size(),
                state.specialCharacters.size(),
                deduplicationResult.containsDuplicates(),
                elapsedMillis(started)
        );
        log.info("event=bfhl_request_processed request_id={} valid={} invalid={} numbers={} alphabets={} specials={} elapsed_ms={}",
                requestId, deduplicationResult.uniqueValues().size(), deduplicationResult.invalidCount(), response.numberCount(),
                response.alphabetCount(), response.specialCharacterCount(), response.processingTimeMs());
        return response;
    }

    void processLargePayloadAsync(BfhlRequest request, String requestId, String correlationId) {
        long started = System.nanoTime();
        BfhlResponse response = processSynchronously(request, requestId, started);
        log.info("event=bfhl_async_payload_completed request_id={} correlation_id={} elapsed_ms={}",
                requestId, correlationId, response.processingTimeMs());
    }

    private DeduplicationResult deduplicate(List<Object> rawValues) {
        Map<String, String> uniqueValues = new LinkedHashMap<>();
        int invalidCount = 0;
        boolean containsDuplicates = false;
        for (Object rawValue : rawValues) {
            if (rawValue == null) {
                invalidCount++;
                continue;
            }
            String normalized = String.valueOf(rawValue).trim();
            if (normalized.isEmpty()) {
                invalidCount++;
                continue;
            }
            String key = normalized.toUpperCase(Locale.ROOT);
            if (uniqueValues.containsKey(key)) {
                containsDuplicates = true;
                continue;
            }
            uniqueValues.put(key, normalized);
        }
        return new DeduplicationResult(new ArrayList<>(uniqueValues.values()), invalidCount, containsDuplicates);
    }

    // private void processValue(String value, ProcessingState state) {
    //     Matcher numberMatcher = NUMBER_PATTERN.matcher(value);
    //     boolean[] consumed = new boolean[value.length()];
    //     Matcher alphabetMatcher = ALPHA_PATTERN.matcher(value);

    //     while (alphabetMatcher.find()) {

    //         String alphabeticValue
    //                 = alphabetMatcher.group().toUpperCase(Locale.ROOT);

    //         state.alphabets.add(alphabeticValue);

    //         for (char c : alphabeticValue.toCharArray()) {
    //             if (isVowel(c)) {
    //                 state.vowelCount++;
    //             }
    //         }
    //     }

    //     while (alphabetMatcher.find()) {

    //         String alphabeticValue
    //                 = alphabetMatcher.group().toUpperCase(Locale.ROOT);

    //         state.alphabets.add(alphabeticValue);

    //         for (char c : alphabeticValue.toCharArray()) {
    //             if (isVowel(c)) {
    //                 state.vowelCount++;
    //             }
    //         }
    //     }

    //     StringBuilder specialBuilder = new StringBuilder();
    //     for (int i = 0; i < value.length(); i++) {
    //         char current = value.charAt(i);
    //         if (!consumed[i] && !Character.isWhitespace(current)) {
    //             specialBuilder.append(current);
    //         }
    //     }
    //     if (!specialBuilder.isEmpty()) {
    //         state.specialCharacters.add(specialBuilder.toString());
    //     }
    // }

    private void processValue(String value, ProcessingState state) {

    boolean[] consumed = new boolean[value.length()];


    if (value.matches("[-+]?\\d+")) {

        BigDecimal number = new BigDecimal(value).stripTrailingZeros();

        state.numericValues.add(number);

        if (isEvenInteger(number)) {
            state.evenNumbers.add(formatNumber(number));
        } else if (isOddInteger(number)) {
            state.oddNumbers.add(formatNumber(number));
        }

        for (int i = 0; i < value.length(); i++) {
            consumed[i] = true;
        }
    }


    Matcher alphabetMatcher = ALPHA_PATTERN.matcher(value);

    while (alphabetMatcher.find()) {

        markConsumed(consumed,
                alphabetMatcher.start(),
                alphabetMatcher.end());

        String alphabeticValue =
                alphabetMatcher.group().toUpperCase(Locale.ROOT);

        state.alphabets.add(alphabeticValue);

        for (char c : alphabeticValue.toCharArray()) {
            if (isVowel(c)) {
                state.vowelCount++;
            }
        }
    }

    StringBuilder specialBuilder = new StringBuilder();

    for (int i = 0; i < value.length(); i++) {

        char current = value.charAt(i);

        if (!consumed[i]
                && !Character.isWhitespace(current)
                && !Character.isDigit(current)) {

            specialBuilder.append(current);
        }
    }

    if (!specialBuilder.isEmpty()) {
        state.specialCharacters.add(specialBuilder.toString());
    }
}

    private void markConsumed(boolean[] consumed, int start, int end) {
        for (int i = start; i < end; i++) {
            consumed[i] = true;
        }
    }

    private boolean isEvenInteger(BigDecimal number) {
        return isWholeNumber(number) && number.remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isOddInteger(BigDecimal number) {
        return isWholeNumber(number) && number.remainder(BigDecimal.valueOf(2)).abs().compareTo(BigDecimal.ONE) == 0;
    }

    private boolean isWholeNumber(BigDecimal number) {
        return number.stripTrailingZeros().scale() <= 0;
    }

    private boolean isVowel(char character) {
        return switch (character) {
            case 'A', 'E', 'I', 'O', 'U' ->
                true;
            default ->
                false;
        };
    }

    private void updateLongestAndShortest(String alphabeticValue, ProcessingState state) {
        if (state.longestAlphabeticValue == null || alphabeticValue.length() > state.longestAlphabeticValue.length()) {
            state.longestAlphabeticValue = alphabeticValue;
        }
        if (state.shortestAlphabeticValue == null || alphabeticValue.length() < state.shortestAlphabeticValue.length()) {
            state.shortestAlphabeticValue = alphabeticValue;
        }
    }

    private String formatNumber(BigDecimal number) {
        BigDecimal normalized = number.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0, RoundingMode.UNNECESSARY);
        }
        return normalized.toPlainString();
    }

    private long elapsedMillis(long started) {
        return Math.max(0, (System.nanoTime() - started) / 1_000_000);
    }

    private record DeduplicationResult(List<String> uniqueValues, int invalidCount, boolean containsDuplicates) {

    }

    private static class ProcessingState {

        private final List<BigDecimal> numericValues = new ArrayList<>();
        private final List<String> oddNumbers = new ArrayList<>();
        private final List<String> evenNumbers = new ArrayList<>();
        private final List<String> alphabets = new ArrayList<>();
        private final List<String> specialCharacters = new ArrayList<>();
        private final Map<String, Long> alphabetFrequency = new LinkedHashMap<>();
        private int vowelCount;
        private String longestAlphabeticValue;
        private String shortestAlphabeticValue;
    }
}
