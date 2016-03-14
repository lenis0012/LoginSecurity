package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ls_players")
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(unique = true, updatable = false)
    private String uniqueUserId;

    @Column
    private String lastName;

    @Column
    private String password;

    @Column
    private int hashingAlgorithm;

    @Version
    @Column(name = "optlock", nullable = false)
    private long version;
}
