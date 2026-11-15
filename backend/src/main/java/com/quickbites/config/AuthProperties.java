package com.quickbites.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quickbites.auth")
public record AuthProperties(long tokenTtlMinutes) {
}
