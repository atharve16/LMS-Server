package com.lms.server.dto;

import java.time.LocalDate;

public record ApplyLeaveRequest(
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {}