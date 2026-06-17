package com.praxx.bfhl.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.praxx.bfhl.config.ApiProperties;
import com.praxx.bfhl.dto.AsyncBfhlResponse;
import com.praxx.bfhl.dto.BfhlRequest;
import com.praxx.bfhl.dto.BfhlResponse;
import com.praxx.bfhl.dto.BfhlResponseContract;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BfhlServiceImplTest {

    private BfhlServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BfhlServiceImpl(new ApiProperties("test_user", "test@example.com", "ROLL123", 10_000));
    }

    @Test
    void processesOriginalAssignmentFieldsAndNewMetrics() {
        BfhlResponse response = syncResponse(service.process(
                new BfhlRequest(List.of("A", "1", "22", "$", "B", "7")),
                "REQ-1001"
        ));

        assertThat(response.success()).isTrue();
        assertThat(response.requestId()).isEqualTo("REQ-1001");
        assertThat(response.oddNumbers()).containsExactly("1", "7");
        assertThat(response.evenNumbers()).containsExactly("22");
        assertThat(response.alphabets()).containsExactly("A", "B");
        assertThat(response.specialCharacters()).containsExactly("$");
        assertThat(response.sum()).isEqualTo("30");
        assertThat(response.largestNumber()).isEqualTo("22");
        assertThat(response.smallestNumber()).isEqualTo("1");
        assertThat(response.alphabetCount()).isEqualTo(2);
        assertThat(response.numberCount()).isEqualTo(3);
        assertThat(response.specialCharacterCount()).isEqualTo(1);
        assertThat(response.containsDuplicates()).isFalse();
    }

    @Test
    void ignoresNullBlankValuesAndDeduplicatesBeforeProcessing() {
        BfhlResponse response = syncResponse(service.process(
                new BfhlRequest(List.of("10", "10", "A", "a", "", "   ", "&", "5")),
                "REQ-1003"
        ));

        assertThat(response.evenNumbers()).containsExactly("10");
        assertThat(response.oddNumbers()).containsExactly("5");
        assertThat(response.alphabets()).containsExactly("A");
        assertThat(response.specialCharacters()).containsExactly("&");
        assertThat(response.sum()).isEqualTo("15");
        assertThat(response.containsDuplicates()).isTrue();
    }

    @Test
    void extractsNumbersLettersAndSpecialsFromMixedStrings() {
        BfhlResponse response = syncResponse(service.process(
                new BfhlRequest(List.of("A1B2", "100", "#", "Test123", "Z", "55", "X@-10.5")),
                "REQ-1002"
        ));

        assertThat(response.sum()).isEqualTo("270.5");
        assertThat(response.evenNumbers()).containsExactly("2", "100");
        assertThat(response.oddNumbers()).containsExactly("1", "123", "55");
        assertThat(response.alphabets()).containsExactly("A", "B", "T", "E", "S", "T", "Z", "X");
        assertThat(response.specialCharacters()).containsExactly("#", "@");
    }

    @Test
    void returnsAcceptedResponseForLargePayloads() {
        BfhlServiceImpl smallThresholdService = new BfhlServiceImpl(new ApiProperties("u", "e", "r", 1));

        BfhlResponseContract response = smallThresholdService.process(new BfhlRequest(List.of("1", "2")), "REQ-LARGE");

        assertThat(response).isInstanceOf(AsyncBfhlResponse.class);
        AsyncBfhlResponse asyncResponse = (AsyncBfhlResponse) response;
        assertThat(asyncResponse.success()).isTrue();
        assertThat(asyncResponse.requestId()).isEqualTo("REQ-LARGE");
        assertThat(asyncResponse.correlationId()).isNotBlank();
        assertThat(asyncResponse.status()).isEqualTo("ACCEPTED");
    }

    private BfhlResponse syncResponse(BfhlResponseContract response) {
        assertThat(response).isInstanceOf(BfhlResponse.class);
        return (BfhlResponse) response;
    }
}
