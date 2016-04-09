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

public class xAuthMigration extends Migration {
    private static final int ACCOUNTS_BATCH_SIZE = 100;

    @Override
    public boolean executeAutomatically() {
        return false;
    }

    @Override
    public boolean canExecute() {
        File file = new File(LoginSecurity.getInstance().getDataFolder(), "xAuth.h2.db");
        return file.exists();
    }

    @Override
    public boolean execute() {
        PluginHolder plugin = LoginSecurity.getInstance();
        final EbeanServer database = plugin.getDatabase();
        final Logger logger = plugin.getLogger();
        final String dbFile = plugin.getDataFolder().getPath() + File.separator + "xAuth";
        final String driver = "org.h2.Driver";
        final String url = "jdbc:h2:" + dbFile + ";MODE=MySQL;IGNORECASE=TRUE";
        final String user = "sa";
        final String password = "";

        // Verify & initiate driver
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Couldn't load H2 driver, is it in your lib folder?");
            return false;
        }

        // Execute
        Connection connection = null;
        Transaction transaction = null;
        this.entriesCompleted = 0;
        try {
            connection = DriverManager.getConnection(url, user, password);
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
                entriesCompleted++;
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
                    if(offline == null) {
                        it.remove(); continue;
                    }

                    UUID id = offline.getUniqueId();
                    if(id == null) {
                        it.remove();
                        continue;
                    }

                    profile.setUniqueUserId(id.toString());
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
                if(entriesCompleted++ >= ACCOUNTS_BATCH_SIZE) {
                    database.commitTransaction();
                    transaction = database.beginTransaction();
                    transaction.setBatchMode(true);
                    transaction.setBatchSize(ACCOUNTS_BATCH_SIZE);
                }

                // status update
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

    @Override
    public String getName() {
        return "xAuth";
    }

    private PlayerProfile getProfile(ResultSet result) throws SQLException {
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
