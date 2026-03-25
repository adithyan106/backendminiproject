package com.campuscomplaint.repository;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Complaint entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    /**
     * Find all complaints with a given status that were created before a specific time.
     * Used by the escalation scheduler to find stale PENDING complaints.
     */
    List<Complaint> findByStatusAndCreatedAtBefore(ComplaintStatus status, LocalDateTime dateTime);
}
