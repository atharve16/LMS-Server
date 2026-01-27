package com.lms.server.dto;

import java.util.UUID;

public record AuthResponse(
        UUID employeeId,
        String name,
        String email,
        String role,
        String token
) {}