package com.lms.server.dto;

public record ReviewLeaveRequest(
        String status,        // approved | rejected
        String reviewComments
) {}