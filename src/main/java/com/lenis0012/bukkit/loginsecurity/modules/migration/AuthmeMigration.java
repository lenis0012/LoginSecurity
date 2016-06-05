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
        final EbeanServer database = plugin.getDatabase();
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
        Transaction transaction = null;
        this.entriesCompleted = 0;
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();

            // Obtain column count
            log("Counting total amount of accounts...");
            ResultSet result = statement.executeQuery("SELECT * FROM accounts;");
            ResultSetMetaData metadata = result.getMetaData();
            this.entriesTotal = metadata.getColumnCount();
            result.close();
            log("Total accounts: " + entriesTotal);

            // Obtain data
            final Set<PlayerProfile> profiles = Sets.newHashSet();
            result = statement.executeQuery("SELECT * FROM accounts;");
            while(result.next()) {
                PlayerProfile profile = getProfile(result);
                if(profile != null) {
                    // Profile is null when authentication method isn't found
                    profiles.add(profile);
                }

                // Progress update
                entriesCompleted += 1;
                progressUpdate("Loading accounts from xAuth");
            }
            result.close();

            // Update entries size
            int removed = entriesTotal - profiles.size();
            this.entriesTotal = profiles.size();
            if(removed > 0) {
                log(removed + " profiles were not loaded because the password type was unsupported!");
            }

            // Obtain UUIDs
            log("Loaded accounts from database, now doing UUID lookup...");
            this.entriesCompleted = 0;
            boolean useOnlineUUID = ProfileUtil.useOnlineUUID();
            Iterator<PlayerProfile> it = profiles.iterator();
            while(it.hasNext()) {
                PlayerProfile profile = it.next();
                if(useOnlineUUID) {
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(profile.getLastName());
                    if(offline == null || offline.getUniqueId() == null) {
                        it.remove();
                        continue;
                    }

                    profile.setUniqueUserId(offline.getUniqueId().toString());
                } else {
                    profile.setUniqueUserId(ProfileUtil.getUUID(profile.getLastName(), null).toString());
                }
            }

            // Update entries size
            removed = entriesTotal - profiles.size();
            this.entriesTotal = profiles.size();
            if(removed > 0) {
                log(removed + " profiles were not loaded because we couldn't get their UUID!");
            }

            // Save data
            log("All entries were loaded, now saving in to new database...");
            entriesCompleted = 0;
            transaction = database.beginTransaction();
            transaction.setBatchMode(true);
            transaction.setBatchSize(ACCOUNTS_BATCH_SIZE);
            for(PlayerProfile profile : profiles) {
                database.save(profile);
                if(++entriesCompleted % ACCOUNTS_BATCH_SIZE == 0) {
                    database.commitTransaction();
                    transaction = database.beginTransaction();
                    transaction.setBatchMode(true);
                    transaction.setBatchSize(ACCOUNTS_BATCH_SIZE);
                }

                // status update authme
                progressUpdate("Inserting accounts in to database");
            }
            database.commitTransaction();
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
            if(transaction != null) {
                database.endTransaction();
            }
        }
    }

    private PlayerProfile getProfile(ResultSet result) throws SQLException {
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
