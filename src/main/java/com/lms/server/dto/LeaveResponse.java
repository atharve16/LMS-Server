package com.lms.server.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LeaveResponse(
        UUID leaveId,
        LocalDate startDate,
        LocalDate endDate,
        int daysRequested,
        String status,
        String reason
) {}