package com.praxx.bfhl.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApplicationConfig {
}
