package com.servicedesk.service;

import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.*;
import com.servicedesk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final ResolutionLogRepository resolutionLogRepository;
    private final BugReportRepository bugReportRepository;

    public DataInitializer(UserRepository userRepository, TicketRepository ticketRepository,
                           TicketCommentRepository commentRepository, ResolutionLogRepository resolutionLogRepository,
                           BugReportRepository bugReportRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.resolutionLogRepository = resolutionLogRepository;
        this.bugReportRepository = bugReportRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 1. Create Users
        Admin admin = new Admin("Sarah Jenkins", "admin@servicedesk.internal", "IT Operations", "SUPER_ADMIN");
        SupportEngineer support1 = new SupportEngineer("Alex Rivera", "alex.rivera@servicedesk.internal", "Technical Support", "Tier 2", "Database & Infrastructure");
        SupportEngineer support2 = new SupportEngineer("Priya Sharma", "priya.sharma@servicedesk.internal", "Technical Support", "Tier 1", "Application & Authentication");
        Developer dev1 = new Developer("David Chen", "david.chen@servicedesk.internal", "Engineering", "Backend Core", "Java, Spring Boot, PostgreSQL");
        Developer dev2 = new Developer("Marcus Vance", "marcus.vance@servicedesk.internal", "Engineering", "Security & IAM", "Java, OAuth2, Spring Security");
        Employee emp1 = new Employee("Emily Watson", "emily.watson@company.internal", "Finance", "Senior Financial Analyst", "HQ Building A - Floor 4");
        Employee emp2 = new Employee("Tom Miller", "tom.miller@company.internal", "Human Resources", "HR Operations Lead", "HQ Building B - Floor 2");

        userRepository.save(admin);
        userRepository.save(support1);
        userRepository.save(support2);
        userRepository.save(dev1);
        userRepository.save(dev2);
        userRepository.save(emp1);
        userRepository.save(emp2);

        // 2. Ticket 1: Application Login Failing (Escalated to Bug SD-1024 / BUG-302)
        Ticket ticket1 = new Ticket("SD-1024", "Application login failing after password reset",
                "Users cannot log in after completing the self-service password reset flow. Server returns HTTP 500 error.",
                TicketPriority.HIGH, TicketCategory.AUTHENTICATION, emp1);
        ticket1.setAssignedTo(support2);
        ticket1.setStatus(TicketStatus.IN_PROGRESS);
        ticket1.setCreatedAt(LocalDateTime.now().minusHours(5));
        ticket1.setUpdatedAt(LocalDateTime.now().minusHours(2));
        ticketRepository.save(ticket1);

        TicketComment comment1 = new TicketComment(ticket1, support2, "Reproduced issue on staging. Exception in AuthTokenService.", true);
        commentRepository.save(comment1);

        BugReport bug1 = new BugReport(
                "BUG-302",
                ticket1,
                "NullPointerException in AuthTokenService during token refresh after reset",
                BugSeverity.CRITICAL,
                "Production",
                "1. Trigger self-service password reset email\n2. Update password to new alphanumeric value\n3. Navigate to /login and submit credentials\n4. Observe 500 Internal Server Error",
                "User is redirected to dashboard with valid JWT bearer token.",
                "HTTP 500: java.lang.NullPointerException at com.company.auth.TokenService.generateRefreshToken()",
                "java.lang.NullPointerException: Cannot invoke 'String.getBytes()' because 'salt' is null\n\tat com.company.auth.TokenService.generateRefreshToken(TokenService.java:84)\n\tat com.company.auth.AuthController.login(AuthController.java:42)",
                dev1
        );
        ticket1.attachBugReport(bug1);
        bugReportRepository.save(bug1);
        ticketRepository.save(ticket1);

        // 3. Ticket 2: Database Connection Failing (SD-1018 - Resolved with detailed troubleshooting log)
        Ticket ticket2 = new Ticket("SD-1018", "Database connection pool exhausted on reporting node",
                "Scheduled quarterly financial report batch failed due to PostgreSQL connection timeout error.",
                TicketPriority.CRITICAL, TicketCategory.DATABASE, emp1);
        ticket2.setAssignedTo(support1);
        ticket2.setStatus(TicketStatus.RESOLVED);
        ticket2.setCreatedAt(LocalDateTime.now().minusDays(1).minusHours(4));
        ticket2.setResolvedAt(LocalDateTime.now().minusDays(1));
        ticket2.setUpdatedAt(LocalDateTime.now().minusDays(1));
        ticketRepository.save(ticket2);

        ResolutionLog resLog2 = new ResolutionLog(
                ticket2,
                "HikariCP connection pool exhausted causing 30s timeout on reporting queries",
                "1. Checked application server log /var/log/reporting.log for stack traces.\n2. Identified HikariPool-1 - Connection is not available, request timed out after 30000ms.\n3. Queried pg_stat_activity on PostgreSQL replica to inspect long-running idle connections.\n4. Discovered unclosed Hibernate Session in QuarterlyReportBatchTask leaving connections in 'idle in transaction'.",
                "Missing @Transactional boundary on stream processing method resulted in leaked JDBC connections that saturated the pool max of 20.",
                "1. Restarted reporting microservice to release stale locks.\n2. Increased HikariCP maximum-pool-size from 20 to 50 in application.properties.\n3. Implemented try-with-resources on the streaming result set and submitted hotfix PR #892.",
                support1
        );
        ticket2.attachResolutionLog(resLog2);
        resolutionLogRepository.save(resLog2);
        ticketRepository.save(ticket2);

        // 4. Ticket 3: VPN Access Request (SD-1030)
        Ticket ticket3 = new Ticket("SD-1030", "Remote VPN Gateway Certificate Request for Contractor",
                "Please provision Cisco AnyConnect certificate profile for new contractor starting next Monday.",
                TicketPriority.MEDIUM, TicketCategory.ACCESS_REQUEST, emp2);
        ticket3.setAssignedTo(support1);
        ticket3.setStatus(TicketStatus.OPEN);
        ticket3.setCreatedAt(LocalDateTime.now().minusHours(2));
        ticketRepository.save(ticket3);

        // 5. Ticket 4: Slow invoice PDF generation (SD-1021)
        Ticket ticket4 = new Ticket("SD-1021", "Slow invoice PDF generation for bulk accounts",
                "Generating PDF invoices for accounts with >500 items takes over 4 minutes to download.",
                TicketPriority.LOW, TicketCategory.APPLICATION_BUG, emp2);
        ticket4.setAssignedTo(support2);
        ticket4.setStatus(TicketStatus.WAITING_FOR_USER);
        ticket4.setCreatedAt(LocalDateTime.now().minusDays(2));
        ticket4.setUpdatedAt(LocalDateTime.now().minusHours(8));
        ticketRepository.save(ticket4);

        TicketComment comment4 = new TicketComment(ticket4, support2, "Could you provide sample invoice IDs that exhibit the slowest generation times?", false);
        commentRepository.save(comment4);

        // 6. Ticket 5: Monitor display flickering (SD-1005)
        Ticket ticket5 = new Ticket("SD-1005", "Dual-monitor display flickering on workstation dock",
                "USB-C docking station disconnects external DisplayPort monitor intermittently.",
                TicketPriority.LOW, TicketCategory.HARDWARE, emp1);
        ticket5.setAssignedTo(support1);
        ticket5.setStatus(TicketStatus.CLOSED);
        ticket5.setCreatedAt(LocalDateTime.now().minusDays(5));
        ticket5.setResolvedAt(LocalDateTime.now().minusDays(4));
        ticket5.setClosedAt(LocalDateTime.now().minusDays(3));
        ticketRepository.save(ticket5);
    }
}
