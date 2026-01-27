package com.lms.server.repository;

import com.lms.server.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Employee> findById(UUID id);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);
}