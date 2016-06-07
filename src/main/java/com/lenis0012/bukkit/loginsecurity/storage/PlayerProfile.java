package com.lenis0012.bukkit.loginsecurity.storage;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

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

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "location_id")
    private PlayerLocation loginLocation;

    @Column
    private Date registrationDate;

    @Version
    @Column(name = "optlock", nullable = false)
    private long version;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniqueUserId() {
        return uniqueUserId;
    }

    public void setUniqueUserId(String uniqueUserId) {
        this.uniqueUserId = uniqueUserId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public void setHashingAlgorithm(int hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public PlayerLocation getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(PlayerLocation loginLocation) {
        this.loginLocation = loginLocation;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
