package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;
import com.google.common.base.Charsets;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
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

    @Override
    public boolean executeAutomatically() {
        final PluginHolder plugin = LoginSecurity.getInstance();
        final StorageModule storage = plugin.getModule(StorageModule.class);
        if(storage.isRunningMySQL()) {
            try {
                SqlQuery query = plugin.getDatabase().createSqlQuery("SELECT COUNT(*) FROM users");
                int rows = (Integer) query.findUnique().values().toArray(new Object[0])[0];
                return rows > 0;
            } catch(Exception e) {
                e.printStackTrace(); //TODO: Remove debug
                return false;
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
            Configuration config = new Configuration(new File(plugin.getDataFolder(), "database.yml"));
            String host = config.getString("mysql.host");
            String user = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            String db = config.getString("mysql.database");
            url = "jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + password;
            driver = "com.mysql.jdbc.Driver";
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
        if(!storage.isRunningMySQL()) {
            try {
                copyFile(file, new File(plugin.getDataFolder(), "users.backup.db"));
            } catch(IOException e) {
                logger.log(Level.WARNING, "Failed to create database backup... let's hope nothing goes wrong then =)", e);
            }
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();

            // Obtain column count
            count(statement, "users");
            Set<PlayerProfile> profiles = loadProfiles(statement, "users");
//            loadUserIds(profiles);
            saveProfiles(profiles, database);

            // Cleanup
            statement.execute("DROP TABLE users;");

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
        final String userId = result.getString("unique_user_id");
        final String hash = result.getString("password");
        final String ipAddress = result.getString("ip");
        final int algorithm = result.getInt("encryption");
        StringBuilder builder = new StringBuilder();
        builder.append(userId.substring(0, 8)).append("-")
                .append(userId.substring(8, 12)).append("-")
                .append(userId.substring(12, 16)).append("-")
                .append(userId.substring(16, 20)).append("-")
                .append(userId.substring(20));

        PlayerProfile profile = new PlayerProfile();
        profile.setUniqueUserId(builder.toString());
        profile.setPassword(hash);
        profile.setHashingAlgorithm(algorithm);
        profile.setIpAddress(ipAddress);
        profile.setRegistrationDate(new Date(System.currentTimeMillis()));
        profile.setLastLogin(new Timestamp(System.currentTimeMillis()));

        // Attempt to get name
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(builder.toString()));
        if(player != null) {
            profile.setLastName(player.getName());
            profile.setUniqueUserId(
                    UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName())
                            .getBytes(Charsets.UTF_8)).toString());
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
}
