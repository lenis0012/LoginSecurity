package com.lenis0012.bukkit.loginsecurity.storage;

import java.sql.Timestamp;

public class Migration {
    private int id;

    private String version;

    private String name;

    private Timestamp appliedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Migration() {}

    public Migration(String version, String name, Timestamp appliedAt) {
        this.version = version;
        this.name = name;
        this.appliedAt = appliedAt;
    }
}
