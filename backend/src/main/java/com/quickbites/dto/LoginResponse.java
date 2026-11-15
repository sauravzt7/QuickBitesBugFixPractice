package com.quickbites.dto;

public record LoginResponse(String token, Long userId, String fullName, String email) {
}
