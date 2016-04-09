package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "ls_players")
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(unique = true, updatable = false, length = 128)
    private String uniqueUserId;

    @Column(length = 16)
    private String lastName;

    @Column
    private String ipAddress;

    @Column(length = 512)
    private String password;

    @Column
    private int hashingAlgorithm;

    @Column
    private Timestamp lastLogin;

    @Column
    private Date registrationDate;

    @Version
    @Column(name = "optlock", nullable = false)
    private long version;
}
