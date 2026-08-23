package com.servicedesk.repository;

import com.servicedesk.domain.Ticket;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority);

    List<Ticket> findByAssignedToIdOrderByCreatedAtDesc(Long assignedToId);

    List<Ticket> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    // Custom SQL Analytics Queries for Reporting and Interview Discussions

    // 1. Group tickets by priority
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.status != 'CLOSED' GROUP BY t.priority")
    List<Object[]> countOpenTicketsByPriority();

    // 2. Group tickets by category
    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> countTicketsByCategory();

    // 3. Support engineer workload
    @Query("SELECT t.assignedTo.name, COUNT(t) FROM Ticket t WHERE t.status IN ('OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER') AND t.assignedTo IS NOT NULL GROUP BY t.assignedTo.name")
    List<Object[]> countWorkloadByEngineer();

    // 4. Status counts
    long countByStatus(TicketStatus status);

    // 5. Critical tickets count
    long countByPriorityAndStatusNot(TicketPriority priority, TicketStatus status);

    // 6. Search tickets by keyword in title, description, or ticketNumber
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<Ticket> searchTickets(@Param("query") String query);

    // 7. Completed tickets for calculating average resolution time
    @Query("SELECT t FROM Ticket t WHERE t.resolvedAt IS NOT NULL")
    List<Ticket> findResolvedTickets();
}
