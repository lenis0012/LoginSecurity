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

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.pluginutils.PluginHolder;

import java.io.File;
import java.sql.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthmeMigration extends AbstractMigration {
    private static final int ACCOUNTS_BATCH_SIZE = 100;

    @Override
    public boolean executeAutomatically() {
        return false;
    }

    @Override
    public boolean canExecute(String[] params) {
        String platform = params.length > 0 ? params[0].toLowerCase() : "sqlite";
        if(platform.equals("mysql")) {
            return params.length > 4;
        } else {
            File file = new File(LoginSecurity.getInstance().getDataFolder(), "authme.db");
            return file.exists();
        }
    }

    @Override
    public boolean execute(String[] params) {
        PluginHolder plugin = LoginSecurity.getInstance();
//        final EbeanServer database = plugin.getDatabase();
        final Logger logger = plugin.getLogger();
        final String dbFile = plugin.getDataFolder().getPath() + File.separator + "authme.db";
        final String platform = params.length > 0 ? params[0].toLowerCase() : "sqlite";
        String driver = "org.sqlite.JDBC";
        String url = "jdbc:sqlite:" + dbFile;
        if(platform.equals("mysql")) {
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + params[1] + "/" + params[4] + "?user=" + params[2] + "&password=" + params[3];
        }

        // Verify & initiate driver
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Couldn't load SQL driver", e);
            return false;
        }

        // Execute
        Connection connection = null;
        this.entriesCompleted = 0;
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();

            // Obtain column count
            count(statement, "authme");
            Set<PlayerProfile> profiles = loadProfiles(statement, "authme");
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
    public PlayerProfile getProfile(ResultSet result) throws SQLException {
        final String name = result.getString("username");
        final String hash = result.getString("password");
        final String ipAddress = result.getString("ip");
        final Timestamp lastLogin = new Timestamp(result.getLong("lastlogin"));
        final Timestamp registration = new Timestamp(System.currentTimeMillis());
        int algorithm = hash.startsWith("$SHA$") ? Algorithm.AuthMe_SHA256.getId() : -1;
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

    @Override
    public String getName() {
        return "AuthMe";
    }

    @Override
    public int minParams() {
        return 0;
    }
}
