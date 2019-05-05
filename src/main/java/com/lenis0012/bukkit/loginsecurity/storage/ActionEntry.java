package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ActionEntry {
    private int id;

    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    private String uniqueUserId;

    private String type;

    private String service;

    private String provider;
}
