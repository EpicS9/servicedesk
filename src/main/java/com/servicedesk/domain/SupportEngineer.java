package com.servicedesk.domain;

import com.servicedesk.domain.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SUPPORT_ENGINEER")
public class SupportEngineer extends User {

    private String tierLevel; // e.g., "Tier 1", "Tier 2", "Tier 3"
    private String specialization; // e.g., "Database & Infrastructure", "Application & Auth"

    public SupportEngineer() {
        super();
        setRole(Role.SUPPORT_ENGINEER);
    }

    public SupportEngineer(String name, String email, String department, String tierLevel, String specialization) {
        super(name, email, Role.SUPPORT_ENGINEER, department);
        this.tierLevel = tierLevel;
        this.specialization = specialization;
    }

    @Override
    public String getRoleDescription() {
        return "Support Engineer (" + tierLevel + ") specializing in " + specialization;
    }

    @Override
    public boolean canBeAssignedTickets() {
        return true;
    }

    @Override
    public boolean canTroubleshoot() {
        return true;
    }

    public String getTierLevel() {
        return tierLevel;
    }

    public void setTierLevel(String tierLevel) {
        this.tierLevel = tierLevel;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
