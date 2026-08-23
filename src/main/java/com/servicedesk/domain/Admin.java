package com.servicedesk.domain;

import com.servicedesk.domain.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    private String accessLevel;

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(String name, String email, String department, String accessLevel) {
        super(name, email, Role.ADMIN, department);
        this.accessLevel = accessLevel;
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator with full access privilege (" + accessLevel + ")";
    }

    @Override
    public boolean canBeAssignedTickets() {
        return true;
    }

    @Override
    public boolean canTroubleshoot() {
        return true;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
