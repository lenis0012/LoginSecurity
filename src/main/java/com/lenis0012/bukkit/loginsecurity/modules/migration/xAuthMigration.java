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

package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.google.common.collect.Sets;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import com.lenis0012.pluginutils.PluginHolder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class xAuthMigration extends AbstractMigration {
    private static final int ACCOUNTS_BATCH_SIZE = 100;

    @Override
    public boolean executeAutomatically() {
        return false;
    }

    @Override
    public boolean canExecute(String[] params) {
        String platform = params.length > 0 ? params[0].toLowerCase() : "h2";
        if(platform.equals("mysql")) {
            return params.length > 4;
        } else {
            File file = new File(LoginSecurity.getInstance().getDataFolder(), "xAuth.h2.db");
            return file.exists();
        }
    }

    @Override
    public boolean execute(String[] params) {
        PluginHolder plugin = LoginSecurity.getInstance();
        final Logger logger = plugin.getLogger();
        final String dbFile = plugin.getDataFolder().getPath() + File.separator + "xAuth.h2.db";
        final String platform = params.length > 0 ? params[0].toLowerCase() : "h2";
        String driver = "org.h2.Driver";
        String url = "jdbc:h2:" + dbFile + ";MODE=MySQL;IGNORECASE=TRUE";
        String user = "sa";
        String password = "";
        if(platform.equals("mysql")) {
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + params[1] + "/" + params[4];
            user = params[2];
            password = params[3];
        }

        // Verify & initiate driver
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Couldn't load H2 driver, is it in your lib folder?");
            return false;
        }

        // Execute
        Connection connection = null;
        this.entriesCompleted = 0;
        try {
            connection = DriverManager.getConnection(url, user, password);
            Statement statement = connection.createStatement();

            // Obtain column count
            count(statement, "accounts");
            Set<PlayerProfile> profiles = loadProfiles(statement, "accounts");
            loadUserIds(profiles);
            saveProfiles(profiles);

            // Complete
            log("Completed!");
            return true;
        } catch(SQLException e) {
            logger.log(Level.SEVERE, "Failed to migrate database", e);
            return false;
        } finally {
            // Release resources
            if(connection != null) {
                try {
                    connection.close();
                } catch(SQLException e) {}
            }
        }
    }

    @Override
    public String getName() {
        return "xAuth";
    }

    @Override
    public int minParams() {
        return 0;
    }

    @Override
    public PlayerProfile getProfile(ResultSet result) throws SQLException {
        final String name = result.getString("playername");
        final String hash = result.getString("password");
        final byte type = result.getByte("pwtype");
        final String ipAddress = result.getString("lastloginip");
        final Timestamp lastLogin = result.getTimestamp("lastlogindate");
        final Timestamp registration = result.getTimestamp("registerdate");
        int algorithm = convertAlgorithm(type);
        if(algorithm == -1) {
            return null;
        }

        PlayerProfile profile = new PlayerProfile();
        profile.setLastName(name);
        profile.setPassword(hash);
        profile.setHashingAlgorithm(algorithm);
        profile.setIpAddress(ipAddress);
        profile.setRegistrationDate(new Date(registration.getTime()));
        profile.setLastLogin(lastLogin);
        return profile;
    }

    private int convertAlgorithm(byte xauthType) {
        switch(xauthType) {
            case 0:
                return Algorithm.xAuth_DEFAULT.getId();
            case 1:
                return Algorithm.xAuth_WHIRLPOOL.getId();
            case 5:
                return Algorithm.xAuth_Authme_SHA256.getId();
            default:
                return -1;
        }
    }
}
