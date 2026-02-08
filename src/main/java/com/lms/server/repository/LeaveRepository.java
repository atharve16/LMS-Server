package com.lms.server.repository;

import com.lms.server.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    List<Leave> findByEmployeeId(UUID employeeId);

    List<Leave> findTop10ByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    long countByEmployeeIdAndStatus(UUID employeeId, String status);

    boolean existsByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            UUID employeeId,
            List<String> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}