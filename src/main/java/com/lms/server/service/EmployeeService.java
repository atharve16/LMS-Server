package com.lms.server.service;

import com.lms.server.dto.EmployeeResponse;
import com.lms.server.dto.LeaveBalanceResponse;
import com.lms.server.dto.LeaveResponse;
import com.lms.server.entity.Employee;
import com.lms.server.repository.EmployeeRepository;
import com.lms.server.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActive()))
                .map(this::mapToResponse)
                .toList();
    }

    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapToResponse(employee);
    }

    public EmployeeResponse addEmployee(Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        // 🔴 REQUIRED DEFAULTS
        if (employee.getIsActive() == null) {
            employee.setIsActive(true);
        }
        if (employee.getLeaveBalance() == null) {
            employee.setLeaveBalance(20);
        }

        employeeRepository.save(employee);
        return mapToResponse(employee);
    }

    public LeaveBalanceResponse getLeaveBalanceByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow();
        return getLeaveBalance(employee.getId());
    }

    public LeaveBalanceResponse getLeaveBalance(UUID employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        long pending = leaveRepository.countByEmployeeIdAndStatus(employeeId, "pending");
        long approved = leaveRepository.countByEmployeeIdAndStatus(employeeId, "approved");
        long rejected = leaveRepository.countByEmployeeIdAndStatus(employeeId, "rejected");

        List<LeaveResponse> recentLeaves =
                leaveRepository.findTop10ByEmployeeIdOrderByCreatedAtDesc(employeeId)
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

        return new LeaveBalanceResponse(
                employee.getName(),
                employee.getLeaveBalance(),
                (int) pending,
                (int) approved,
                (int) rejected,
                recentLeaves
        );
    }

    private EmployeeResponse mapToResponse(Employee e) {
        return EmployeeResponse.builder()
                .employeeId(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .department(e.getDepartment())
                .joiningDate(e.getJoiningDate())
                .role(e.getRole())
                .leaveBalance(e.getLeaveBalance())
                .build();
    }
}