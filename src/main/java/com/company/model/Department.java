package com.company.model;

public class Department {
    private Long id;
    private String name;

    public Department() {}

    public Department(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

