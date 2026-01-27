package com.lms.server.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String name,
        String email,
        String password,
        String department,
        String role,
        LocalDate joiningDate
) {}