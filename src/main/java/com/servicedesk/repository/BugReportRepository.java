package com.servicedesk.repository;

import com.servicedesk.domain.BugReport;
import com.servicedesk.domain.enums.BugStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BugReportRepository extends JpaRepository<BugReport, Long> {
    Optional<BugReport> findByBugKey(String bugKey);
    Optional<BugReport> findByTicketId(Long ticketId);
    List<BugReport> findByStatusOrderByCreatedAtDesc(BugStatus status);
    List<BugReport> findAllByOrderByCreatedAtDesc();
    long countByStatus(BugStatus status);
}
