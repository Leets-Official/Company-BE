package com.company.model;

public class Employee {
    private Long id;
    private String name;
    private Long departmentId;
    private int salary;
    private Long managerId;

    public Employee() {}

    public Employee(Long id, String name, Long departmentId, int salary, Long managerId) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.salary = salary;
        this.managerId = managerId;
    }

    public Employee(String name, Long departmentId, int salary, Long managerId) {
        this(null, name, departmentId, salary, managerId);
    }

    public String getName() {
        return name;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public int getSalary() {
        return salary;
    }

    public Long getManagerId() {
        return managerId;
    }
}
