package com.lms.server.dto;

import java.util.List;

public record LeaveBalanceResponse(
        String employeeName,
        int currentBalance,
        int pending,
        int approved,
        int rejected,
        List<LeaveResponse> recentLeaves
) {}