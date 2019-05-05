package com.lenis0012.bukkit.loginsecurity.storage;

import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
public class PlayerProfile {
    private int id;

    private String uniqueUserId;

    private UserIdMode uniqueIdMode = UserIdMode.UNKNOWN;

    private String lastName;

    private String ipAddress;

    private String password;

    private int hashingAlgorithm;

    private Integer inventoryId;

    private Integer loginLocationId;

    private Timestamp lastLogin = new Timestamp(System.currentTimeMillis());

    private Date registrationDate = new Date(System.currentTimeMillis());

    private long version;
}
