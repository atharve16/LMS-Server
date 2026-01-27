package com.lms.server.dto;

public record LoginRequest(
        String email,
        String password
) {}