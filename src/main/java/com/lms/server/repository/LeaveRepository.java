package com.lms.server.repository;

import com.lms.server.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    List<Leave> findByEmployeeId(UUID employeeId);

    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID employeeId,
            List<String> status,
            LocalDate endDate,
            LocalDate startDate
    );

    List<Leave> findTop10ByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);
    long countByEmployeeIdAndStatus(UUID employeeId, String status);
}