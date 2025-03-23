package com.company.model;

public class Project {
    private Long id;
    private String name;
    private int budget;

    public Project() {}

    public Project(Long id, String name, int budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
    }

    public Project(String name, int budget) {
        this(null, name, budget);
    }


    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }
}
