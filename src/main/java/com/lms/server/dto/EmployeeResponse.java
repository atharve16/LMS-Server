package com.lms.server.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String name,
        String email,
        String department,
        String role,
        LocalDate joiningDate,
        int leaveBalance
) {}