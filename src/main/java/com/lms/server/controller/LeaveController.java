package com.lms.server.controller;

import com.lms.server.dto.ApplyLeaveRequest;
import com.lms.server.dto.LeaveResponse;
import com.lms.server.dto.ReviewLeaveRequest;
import com.lms.server.entity.Employee;
import com.lms.server.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasRole('Employee') or hasRole('HR') or hasRole('Admin')")
    public LeaveResponse applyLeave(
            Authentication auth,
            @RequestBody ApplyLeaveRequest request
    ) {
        Employee employee = (Employee) auth.getPrincipal();
        return leaveService.applyLeave(employee.getId(), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('Employee') or hasRole('HR') or hasRole('Admin')")
    public List<LeaveResponse> getMyLeaves(Authentication auth) {
        Employee employee = (Employee) auth.getPrincipal();
        return leaveService.getMyLeaves(employee.getId());
    }

    @PatchMapping("/{leaveId}/review")
    @PreAuthorize("hasRole('HR') or hasRole('Admin')")
    public LeaveResponse reviewLeave(
            Authentication auth,
            @PathVariable UUID leaveId,
            @RequestBody ReviewLeaveRequest request
    ) {
        Employee reviewer = (Employee) auth.getPrincipal();
        return leaveService.reviewLeave(
                reviewer.getId(),
                leaveId,
                request.status(),
                request.reviewComments()
        );
    }
}