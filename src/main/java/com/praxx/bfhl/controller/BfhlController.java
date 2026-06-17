package com.praxx.bfhl.controller;

import com.praxx.bfhl.dto.AsyncBfhlResponse;
import com.praxx.bfhl.dto.BfhlRequest;
import com.praxx.bfhl.dto.BfhlResponseContract;
import com.praxx.bfhl.dto.HealthResponse;
import com.praxx.bfhl.service.BfhlService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BfhlController {

    private static final Logger log = LoggerFactory.getLogger(BfhlController.class);

    private final BfhlService bfhlService;

    public BfhlController(BfhlService bfhlService) {
        this.bfhlService = bfhlService;
    }

    @PostMapping("/bfhl")
    public ResponseEntity<BfhlResponseContract> process(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody BfhlRequest request
    ) {
        log.info("event=bfhl_request_received request_id={} elements={}", requestId, request.data().size());
        BfhlResponseContract response = bfhlService.process(request, requestId);
        HttpStatus status = response instanceof AsyncBfhlResponse ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "bfhl-api");
    }
}
