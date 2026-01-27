package com.lms.server.service;

import com.lms.server.dto.*;
import com.lms.server.entity.Employee;
import com.lms.server.repository.EmployeeRepository;
import com.lms.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {

        if (employeeRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        Employee employee = Employee.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .department(request.department())
                .role(request.role() == null ? "Employee" : request.role())
                .joiningDate(request.joiningDate())
                .leaveBalance(20)
                .build();

        employeeRepository.save(employee);

        String token = JwtUtil.generateToken(employee.getId());

        return new AuthResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole(),
                token
        );
    }

    public AuthResponse login(LoginRequest request) {

        Employee employee = employeeRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = JwtUtil.generateToken(employee.getId());

        return new AuthResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole(),
                token
        );
    }
}