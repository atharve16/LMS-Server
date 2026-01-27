package com.lms.server.controller;

import com.lms.server.dto.ApplyLeaveRequest;
import com.lms.server.dto.LeaveResponse;
import com.lms.server.security.CustomUserDetails;
import com.lms.server.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.lms.server.dto.ReviewLeaveRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public LeaveResponse applyLeave(
            @RequestBody ApplyLeaveRequest request,
            Authentication authentication
    ) {
        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return leaveService.applyLeave(user.getId(), request);
    }

    @GetMapping("/my")
    public List<LeaveResponse> myLeaves(Authentication authentication) {
        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        return leaveService.getMyLeaves(user.getId());
    }

    @PatchMapping("/{leaveId}/review")
    @PreAuthorize("hasRole('HR') or hasRole('Admin')")
    public LeaveResponse reviewLeave(
            @PathVariable UUID leaveId,
            @RequestBody ReviewLeaveRequest request,
            Authentication authentication
    ) {
        CustomUserDetails reviewer =
                (CustomUserDetails) authentication.getPrincipal();

        return leaveService.reviewLeave(
                reviewer.getId(),
                leaveId,
                request.status(),
                request.reviewComments()
        );
    }
}