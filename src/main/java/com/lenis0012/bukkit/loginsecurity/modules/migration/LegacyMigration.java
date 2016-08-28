package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Transaction;
import com.google.common.base.Charsets;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.sql.*;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LegacyMigration extends AbstractMigration {
    private boolean userIdExists;

    @Override
    public boolean executeAutomatically() {
        final PluginHolder plugin = LoginSecurity.getInstance();
        final StorageModule storage = plugin.getModule(StorageModule.class);
        if(storage.isRunningMySQL()) {
            final EbeanServer database = storage.getDatabase();
            Transaction transaction = database.beginTransaction();
            try {
                Connection connection = transaction.getConnection();
                Statement statement = connection.createStatement();
                final ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM users;");
                if(result.next() && result.getInt(1) > 0) {
                    this.userIdExists = columnExists(connection, "unique_user_id");
                    return true;
                } else {
                    return false;
                }
            } catch(SQLException e) {
//                plugin.getLogger().log(Level.WARNING, "Couldn't check mysql database", e);
                return false;
            } finally {
                database.endTransaction();
            }
        } else {
            final File file = new File(plugin.getDataFolder(), "users.db");
            return file.exists();
        }
    }

    @Override
    public boolean canExecute(String[] params) {
        return false;
    }

    @Override
    public boolean execute(String[] params) {
        final PluginHolder plugin = LoginSecurity.getInstance();
        final EbeanServer database = plugin.getDatabase();
        final StorageModule storage = plugin.getModule(StorageModule.class);
        final File file = new File(plugin.getDataFolder(), "users.db");
        final Logger logger = plugin.getLogger();

        String url = "jdbc:sqlite:" + file.getPath();
        String driver = "org.sqlite.JDBC";
        if(storage.isRunningMySQL()) {
            // Run using ebean
            Transaction transaction = database.beginTransaction();
            Set<PlayerProfile> profiles;
            try {
                Connection connection = transaction.getConnection();
                Statement statement = connection.createStatement();
                count(statement, "users");
                profiles = loadProfiles(statement, "users");
            } catch(SQLException e) {
                logger.log(Level.SEVERE, "Failed to migrate database", e);
                return false;
            } finally {
                database.endTransaction();
            }

            // Save
            saveProfiles(profiles, database);

            // Cleanup
            transaction = database.beginTransaction();
            try {
                Connection connection = transaction.getConnection();
                Statement statement = connection.createStatement();
                statement.execute("DROP TABLE users;");
            } catch(SQLException e) {
                logger.log(Level.SEVERE, "Failed to migrate database", e);
                return false;
            } finally {
                database.endTransaction();
            }

            log("Complete!");
            return true;
        }

        // Load driver
        log("Loading driver...");
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to load SQL driver.");
            return false;
        }

        log("Creating backup...");
        try {
            copyFile(file, new File(plugin.getDataFolder(), "users.backup.db"));
        } catch(IOException e) {
            logger.log(Level.WARNING, "Failed to create database backup... let's hope nothing goes wrong then =)", e);
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
            this.userIdExists = columnExists(connection, "unique_user_id");
            Statement statement = connection.createStatement();

            // Obtain column count
            count(statement, "users");
            Set<PlayerProfile> profiles = loadProfiles(statement, "users");
            if(!userIdExists) {
                loadUserIds(profiles);
            }
            saveProfiles(profiles, database);

            log("Complete!");
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

        // Attempt to delete the old file...
        if(!file.delete() ) {
            file.deleteOnExit();
        }

        return true;
    }

    @Override
    public PlayerProfile getProfile(ResultSet result) throws SQLException {
        final String hash = result.getString("password");
        final String ipAddress = result.getString("ip");
        final int algorithm = result.getInt("encryption");

        PlayerProfile profile = new PlayerProfile();
        profile.setPassword(hash);
        profile.setHashingAlgorithm(algorithm);
        profile.setIpAddress(ipAddress);
        profile.setRegistrationDate(new Date(System.currentTimeMillis()));
        profile.setLastLogin(new Timestamp(System.currentTimeMillis()));

        if(userIdExists) {
            final String userId = result.getString("unique_user_id");
            StringBuilder builder = new StringBuilder();
            builder.append(userId.substring(0, 8)).append("-")
                    .append(userId.substring(8, 12)).append("-")
                    .append(userId.substring(12, 16)).append("-")
                    .append(userId.substring(16, 20)).append("-")
                    .append(userId.substring(20));
            profile.setUniqueUserId(builder.toString());

            // Attempt to get name
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(builder.toString()));
            if (player != null) {
                profile.setLastName(player.getName());
                if (!ProfileUtil.useOnlineUUID()) {
                    profile.setUniqueUserId(
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName())
                                    .getBytes(Charsets.UTF_8)).toString());
                }
            }
        } else {
            final String name = result.getString("username");
            profile.setLastName(name);
        }

        return profile;
    }

    @Override
    public String getName() {
        return "Legacy";
    }

    @Override
    public int minParams() {
        return 0; //
    }

    public boolean columnExists(Connection connection, String columnName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rs = meta.getColumns(null, null, "users", columnName);
        boolean success = rs.next();
        rs.close();
        return success;
    }
}
