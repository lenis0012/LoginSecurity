package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ls_players")
@Data
public class PlayerProfile {
    @Column(unique = true, updatable = false)
    private String uniqueUserId;

    @Column
    private String lastName;

    @Column
    private String password;

    @Column
    private int hashingAlgorithm;
}
