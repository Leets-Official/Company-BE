package com.company.dto;

public class DepartmentStats {
    private final String department;
    private final int avgSalary;
    private final int employeeCount;

    public DepartmentStats(String department, int avgSalary, int employeeCount) {
        this.department = department;
        this.avgSalary = avgSalary;
        this.employeeCount = employeeCount;
    }

    public String getDepartment() {
        return department;
    }

    public int getAvgSalary() {
        return avgSalary;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }
}
