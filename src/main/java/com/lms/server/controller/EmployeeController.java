package com.lms.server.controller;

import com.lms.server.dto.EmployeeResponse;
import com.lms.server.dto.LeaveBalanceResponse;
import com.lms.server.entity.Employee;
import com.lms.server.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('Admin')")
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Employee') or hasRole('HR') or hasRole('Admin')")
    public EmployeeResponse getEmployee(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('HR') or hasRole('Admin')")
    public EmployeeResponse addEmployee(@RequestBody Employee employee) {
        return employeeService.addEmployee(employee);
    }

    @GetMapping("/me/leave-balance")
    @PreAuthorize("hasRole('Employee') or hasRole('HR') or hasRole('Admin')")
    public LeaveBalanceResponse getMyLeaveBalance(Authentication auth) {
        Employee employee = (Employee) auth.getPrincipal();
        return employeeService.getLeaveBalance(employee.getId());
    }
}