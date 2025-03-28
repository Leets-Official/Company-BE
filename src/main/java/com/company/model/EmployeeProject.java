package com.company.model;

public class EmployeeProject {
    private Long employeeId;
    private Long projectId;
    private String role;

    public EmployeeProject() {}

    public EmployeeProject(Long employeeId, Long projectId, String role){
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.role = role;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getRole() {
        return role;
    }
}
