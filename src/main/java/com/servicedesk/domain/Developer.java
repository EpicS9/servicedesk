package com.servicedesk.domain;

import com.servicedesk.domain.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DEVELOPER")
public class Developer extends User {

    private String team; // e.g., "Backend Core", "Payments", "Platform"
    private String techStack; // e.g., "Java, Spring Boot, PostgreSQL"

    public Developer() {
        super();
        setRole(Role.DEVELOPER);
    }

    public Developer(String name, String email, String department, String team, String techStack) {
        super(name, email, Role.DEVELOPER, department);
        this.team = team;
        this.techStack = techStack;
    }

    @Override
    public String getRoleDescription() {
        return "Software Developer in team " + team + " (" + techStack + ")";
    }

    @Override
    public boolean canBeAssignedTickets() {
        return true; // Developers can take escalated bug tickets
    }

    @Override
    public boolean canTroubleshoot() {
        return true;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }
}
