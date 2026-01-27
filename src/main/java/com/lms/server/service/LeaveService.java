package com.lms.server.service;

import com.lms.server.dto.ApplyLeaveRequest;
import com.lms.server.dto.LeaveResponse;
import com.lms.server.entity.Employee;
import com.lms.server.entity.Leave;
import com.lms.server.repository.EmployeeRepository;
import com.lms.server.repository.LeaveRepository;
import com.lms.server.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveResponse applyLeave(UUID employeeId, ApplyLeaveRequest request) {

        if (request.startDate().isAfter(request.endDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (request.startDate().isBefore(employee.getJoiningDate())) {
            throw new RuntimeException("Cannot apply leave before joining date");
        }

        int daysRequested =
                DateUtil.calculateBusinessDays(request.startDate(), request.endDate());

        if (daysRequested > employee.getLeaveBalance()) {
            throw new RuntimeException("Insufficient leave balance");
        }

        boolean overlapExists =
                leaveRepository.existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId,
                        List.of("pending", "approved"),
                        request.endDate(),
                        request.startDate()
                );

        if (overlapExists) {
            throw new RuntimeException("Leave overlaps with existing leave");
        }

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reason(request.reason())
                .status("pending")
                .daysRequested(daysRequested)
                .build();

        leaveRepository.save(leave);

        return new LeaveResponse(
                leave.getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getDaysRequested(),
                leave.getStatus(),
                leave.getReason()
        );
    }

    public List<LeaveResponse> getMyLeaves(UUID employeeId) {
        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(l -> new LeaveResponse(
                        l.getId(),
                        l.getStartDate(),
                        l.getEndDate(),
                        l.getDaysRequested(),
                        l.getStatus(),
                        l.getReason()
                ))
                .toList();
    }

    @Transactional
    public LeaveResponse reviewLeave(
            UUID reviewerId,
            UUID leaveId,
            String status,
            String comments
    ) {

        if (!status.equals("approved") && !status.equals("rejected")) {
            throw new RuntimeException("Status must be approved or rejected");
        }

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!leave.getStatus().equals("pending")) {
            throw new RuntimeException("Leave already reviewed");
        }

        Employee employee = employeeRepository.findById(
                leave.getEmployee().getId()
        ).orElseThrow(() -> new RuntimeException("Employee not found"));

        // Prevent self-approval
        if (employee.getId().equals(reviewerId)) {
            throw new RuntimeException("Cannot approve your own leave");
        }

        leave.setStatus(status);
        leave.setReviewedAt(java.time.LocalDateTime.now());
        leave.setReviewComments(comments);

        if (status.equals("approved")) {

            if (employee.getLeaveBalance() < leave.getDaysRequested()) {
                throw new RuntimeException("Insufficient leave balance");
            }

            employee.setLeaveBalance(
                    employee.getLeaveBalance() - leave.getDaysRequested()
            );

            employeeRepository.save(employee);
        }

        leaveRepository.save(leave);

        return new LeaveResponse(
                leave.getId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getDaysRequested(),
                leave.getStatus(),
                leave.getReason()
        );
    }
}