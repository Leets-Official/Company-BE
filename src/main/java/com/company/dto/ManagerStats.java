package com.company.dto;

public class ManagerStats {
    private final String manager;
    private final int subordinateCount;

    public ManagerStats(String manager, int subordinateCount) {
        this.manager = manager;
        this.subordinateCount = subordinateCount;
    }

    public String getManager() {
        return manager;
    }

    public int getSubordinateCount() {
        return subordinateCount;
    }
}
