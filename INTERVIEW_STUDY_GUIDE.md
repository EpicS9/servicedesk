# 🎓 ServiceDesk — Technical Interview & Concept Mapping Guide

This guide maps every module in the **ServiceDesk** application directly to the technical requirements and interview questions in the target Job Description (Java Application Development, OOP, SQL, Testing/QA, Debugging/Troubleshooting, Reporting, and CI/CD).

---

## 1. Object-Oriented Programming (OOP) in Action

### A. Abstraction
- **Code Reference**: [`User.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/User.java)
- **Concept**: An abstract class cannot be instantiated directly; it defines a blueprint for all system users with abstract methods:
  ```java
  public abstract class User {
      public abstract String getRoleDescription();
      public abstract boolean canBeAssignedTickets();
      public abstract boolean canTroubleshoot();
  }
  ```
- **Interview Answer**: *"I used an abstract `User` base class to represent common state (ID, name, email, department) while enforcing role-specific behavior like ticket assignability and troubleshooting authority in concrete subclasses."*

### B. Inheritance & Polymorphism
- **Code Reference**: [`Admin.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/Admin.java), [`SupportEngineer.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/SupportEngineer.java), [`Developer.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/Developer.java), [`Employee.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/Employee.java)
- **Concept**: Subclasses inherit base state and override abstract methods polymorphically:
  - `SupportEngineer.canBeAssignedTickets()` returns `true`.
  - `Employee.canBeAssignedTickets()` returns `false` (employees file tickets rather than receive assignments).
- **Interview Answer**: *"When a ticket is assigned, the service verifies `user.canBeAssignedTickets()` polymorphically without needing `instanceof` cascades or switch blocks."*

### C. Encapsulation
- **Code Reference**: [`Ticket.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/main/java/com/servicedesk/domain/Ticket.java)
- **Concept**: Internal properties are private. State mutations happen through business methods that guarantee consistency and record audit trails:
  ```java
  public void assignTo(User user, User performedBy) {
      if (user != null && !user.canBeAssignedTickets()) {
          throw new IllegalArgumentException("User cannot be assigned tickets");
      }
      this.assignedTo = user;
      if (this.status == TicketStatus.OPEN && user != null) {
          this.status = TicketStatus.IN_PROGRESS;
      }
      this.addHistory(performedBy, "assignedTo", oldVal, user.getName());
  }
  ```

---

## 2. SQL & Relational Database Design

### Query 1: Open Tickets Grouped by Priority
```sql
SELECT priority, COUNT(*) AS ticket_count
FROM tickets
WHERE status != 'CLOSED'
GROUP BY priority
ORDER BY ticket_count DESC;
```
- **Interview Talking Point**: Explains `WHERE` filtering prior to aggregation, `GROUP BY`, and ordering to identify urgent support backlogs.

### Query 2: Support Engineer Active Workload (JOIN)
```sql
SELECT u.name AS engineer_name, COUNT(t.id) AS active_tickets
FROM users u
INNER JOIN tickets t ON t.assigned_to_id = u.id
WHERE t.status IN ('OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER')
GROUP BY u.name
ORDER BY active_tickets DESC;
```
- **Interview Talking Point**: Demonstrates `INNER JOIN` between primary/foreign keys (`users.id` and `tickets.assigned_to_id`) with multiple status filtering.

### Query 3: Average Resolution Time (Date/Time Functions)
```sql
SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - created_at)) / 3600.0) AS avg_hours
FROM tickets
WHERE resolved_at IS NOT NULL;
```
- **Interview Talking Point**: Demonstrates timestamp arithmetic, epoch conversion, and calculating enterprise SLA resolution averages.

### Query 4: Defect Escalation Percentage (LEFT JOIN & Subqueries)
```sql
SELECT 
    COUNT(t.id) AS total_tickets,
    COUNT(b.id) AS converted_bugs,
    ROUND((COUNT(b.id)::DECIMAL / COUNT(t.id)) * 100, 2) AS escalation_percentage
FROM tickets t
LEFT JOIN bug_reports b ON b.ticket_id = t.id;
```
- **Interview Talking Point**: Explains `LEFT JOIN` to preserve tickets without bugs, decimal casting to prevent integer division, and calculating escalation KPIs.

---

## 3. Troubleshooting & Root Cause Analysis (RCA)

### Interview Scenario: "Walk me through how you troubleshoot a production incident."
**Your Answer Using This Project**:
1. **Symptom Identification**: An employee reported batch reporting failures with HTTP 500.
2. **Log Inspection**: Examined `/var/log/reporting.log` and found `HikariPool-1 - Connection is not available, request timed out after 30000ms`.
3. **Database Diagnostics**: Queried `pg_stat_activity` on the PostgreSQL database to inspect active vs. idle connections and detected 20 sessions stuck in `idle in transaction`.
4. **Root Cause Analysis**: Identified a missing `@Transactional` boundary and unclosed `ResultSet` stream in a batch worker task that leaked JDBC connections.
5. **Remediation & Fix**:
   - Short-term: Restarted service to drop stale locks and tuned `maximum-pool-size` from 20 to 50 in `application.yml`.
   - Long-term: Implemented Java `try-with-resources` to ensure auto-closing of database resources and added an automated regression test.

---

## 4. QA Testing & Test Automation

### Automated Testing Structure
- **Unit Testing**: [`TicketServiceTest.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/test/java/com/servicedesk/service/TicketServiceTest.java) using **JUnit 5** and **Mockito**:
  - `@ExtendWith(MockitoExtension.class)`
  - `@Mock` and `@InjectMocks` to isolate dependencies.
  - Assertions with **AssertJ** (`assertThat(...)`, `assertThatThrownBy(...)`).
- **REST Controller Integration Testing**: [`TicketControllerIntegrationTest.java`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/src/test/java/com/servicedesk/controller/TicketControllerIntegrationTest.java) using **MockMvc**:
  - Verifies Bean Validation (`@NotBlank`, `@Size`, `@NotNull`) returning `400 Bad Request`.
  - Verifies correct HTTP status codes (`201 Created`, `200 OK`, `404 Not Found`).

---

## 5. CI/CD & Build Automation

- **Pipeline**: [`.github/workflows/ci.yml`](file:///home/roopesh-baliga/.gemini/antigravity/scratch/servicedesk/.github/workflows/ci.yml)
- **Workflow**:
  1. Trigger on every `git push` or `pull_request` to `main`/`master`.
  2. Setup Java 21 Temurin environment with Maven dependency caching.
  3. Execute automated test suite: `mvn clean test`.
  4. Build production package: `mvn package -DskipTests=true`.
  5. Archive Surefire test reports and executable JAR artifacts.
