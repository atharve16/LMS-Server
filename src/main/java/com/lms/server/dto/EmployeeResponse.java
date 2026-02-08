package com.lms.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
    private UUID employeeId;
    private String name;
    private String email;
    private String department;
    private LocalDate joiningDate;
    private String role;
    private Integer leaveBalance;
}