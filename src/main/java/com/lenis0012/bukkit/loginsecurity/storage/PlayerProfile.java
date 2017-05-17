/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lenis0012.bukkit.loginsecurity.storage;

import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;

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

    @Column(unique = true, length = 128)
    private String uniqueUserId;

    @Column(name = "uuid_mode")
    private UserIdMode uniqueIdMode = UserIdMode.UNKNOWN;

    @Column(length = 16)
    private String lastName;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 512)
    private String password;

    @Column
    private int hashingAlgorithm;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private PlayerLocation loginLocation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "inventory_id")
    private PlayerInventory inventory;

    @Column
    private Timestamp lastLogin = new Timestamp(System.currentTimeMillis());

    @Column
    private Date registrationDate = new Date(System.currentTimeMillis());

    @Version
    @Column(name = "optlock")
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

    public PlayerInventory getInventory() {
        return inventory;
    }

    public void setInventory(PlayerInventory inventory) {
        this.inventory = inventory;
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

    public UserIdMode getUniqueIdMode() {
        return uniqueIdMode;
    }

    public void setUniqueIdMode(UserIdMode uniqueIdMode) {
        this.uniqueIdMode = uniqueIdMode;
    }
}
