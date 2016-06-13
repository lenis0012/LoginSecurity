package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "ls_actions")
public class ActionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private Timestamp timestamp;

    @Column
    private String uniqueUserId;

    @Column
    private String type;

    @Column
    private String service;

    @Column
    private String provider;
}
