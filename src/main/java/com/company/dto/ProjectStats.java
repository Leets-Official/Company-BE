package com.company.dto;

public class ProjectStats {
    private final String project;
    private final int employeeCount;
    private final int totalSalary;

    public ProjectStats(String project, int employeeCount, int totalSalary) {
        this.project = project;
        this.employeeCount = employeeCount;
        this.totalSalary = totalSalary;
    }

    public String getProject() {
        return project;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public int getTotalSalary() {
        return totalSalary;
    }
}
