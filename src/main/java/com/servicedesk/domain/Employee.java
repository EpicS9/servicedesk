package com.servicedesk.domain;

import com.servicedesk.domain.enums.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    private String jobTitle;
    private String officeLocation;

    public Employee() {
        super();
        setRole(Role.EMPLOYEE);
    }

    public Employee(String name, String email, String department, String jobTitle, String officeLocation) {
        super(name, email, Role.EMPLOYEE, department);
        this.jobTitle = jobTitle;
        this.officeLocation = officeLocation;
    }

    @Override
    public String getRoleDescription() {
        return "Enterprise Employee (" + jobTitle + " - " + officeLocation + ")";
    }

    @Override
    public boolean canBeAssignedTickets() {
        return false; // Regular employees file tickets, not receive assignments
    }

    @Override
    public boolean canTroubleshoot() {
        return false;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }
}
