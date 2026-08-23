package com.servicedesk.repository;

import com.servicedesk.domain.ResolutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResolutionLogRepository extends JpaRepository<ResolutionLog, Long> {
    Optional<ResolutionLog> findByTicketId(Long ticketId);
}
