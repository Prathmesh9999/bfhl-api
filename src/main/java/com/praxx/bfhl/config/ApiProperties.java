package com.praxx.bfhl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bfhl")
public record ApiProperties(
        String userId,
        String email,
        String rollNumber,
        int asyncThreshold
) {
}
