package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.google.common.collect.Sets;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.persistence.PersistenceException;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractMigration {
    protected static final int ACCOUNTS_BATCH_SIZE = 100;
    private static final String PROGRESS_FORMAT = "Migrating %s: %s [%s]";

    private long progressUpdateFrequency = 2000L;
    private long nextProgressUpdate = 0L;

    protected int entriesCompleted = 0;
    protected int entriesTotal = 0;

    public abstract boolean executeAutomatically();

    public abstract boolean canExecute(String[] params);

    public abstract boolean execute(String[] params);

    public abstract String getName();

    public abstract int minParams();

    public PlayerProfile getProfile(ResultSet result) throws SQLException {
        throw new IllegalStateException("Didn't override getProfile!");
    }

    protected void saveProfiles(Set<PlayerProfile> profiles, EbeanServer database) {
        log("All entries were loaded, now saving in to new database...");
        Transaction transaction = null;
        this.entriesCompleted = 0;
        try {
            transaction = database.beginTransaction();
            transaction.setBatchMode(true);
            transaction.setBatchSize(ACCOUNTS_BATCH_SIZE);
            for(PlayerProfile profile : profiles) {
                PlayerProfile current = database.find(PlayerProfile.class).where().ieq("unique_user_id", profile.getUniqueUserId()).findUnique();
                if(current != null) {
                    database.delete(current);
                }
                database.save(profile);
                if(++entriesCompleted % ACCOUNTS_BATCH_SIZE == 0) {
                    database.commitTransaction();
                    if(entriesCompleted != entriesTotal) {
                        transaction = database.beginTransaction();
                        transaction.setBatchMode(true);
                        transaction.setBatchSize(ACCOUNTS_BATCH_SIZE);
                    }
                }

                // status update
                progressUpdate("Inserting accounts in to database");
            }
            if(++entriesCompleted % ACCOUNTS_BATCH_SIZE != 0) {
                database.commitTransaction();
            }
        } catch(PersistenceException e) {
            if(transaction != null) {
                database.endTransaction();
            }
        }
    }

    protected void loadUserIds(Set<PlayerProfile> profiles) {
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
                profile.setUniqueIdMode(UserIdMode.MOJANG);
            } else {
                profile.setUniqueUserId(ProfileUtil.getUUID(profile.getLastName(), null).toString());
                profile.setUniqueIdMode(UserIdMode.OFFLINE);
            }
        }

        int removed = entriesTotal - profiles.size();
        this.entriesTotal = profiles.size();
        if(removed > 0) {
            log(removed + " profiles were not loaded because we couldn't get their UUID!");
        }
    }

    protected Set<PlayerProfile> loadProfiles(Statement statement, String table) throws SQLException {
        final Set<PlayerProfile> profiles = Sets.newHashSet();
        ResultSet result = statement.executeQuery("SELECT * FROM " + table + ";");
        while(result.next()) {
            PlayerProfile profile = getProfile(result);
            if(profile != null) {
                // Profile is null when authentication method isn't found
                profiles.add(profile);
            }

            // Progress update
            entriesCompleted += 1;
            progressUpdate("Loading accounts from " + getName());
        }
        result.close();

        // Update count
        int removed = entriesTotal - profiles.size();
        this.entriesTotal = profiles.size();
        if(removed > 0) {
            log(removed + " profiles were not loaded because the password type was unsupported!");
        }
        return profiles;
    }

    protected void count(Statement statement, String table) throws SQLException {
        log("Counting total amount of accounts...");
        ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table + ";");
        this.entriesTotal = result.getInt(1);
        result.close();
        log("Total accounts: " + entriesTotal);
    }

    protected void progressUpdate(String status) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        if(nextProgressUpdate <= System.currentTimeMillis()) {
            nextProgressUpdate = System.currentTimeMillis() + progressUpdateFrequency;
            double progress = (entriesCompleted / (double) entriesTotal) * 100.0;
            String progressText = String.valueOf((int) Math.round(progress)) + "%";
            logger.log(Level.INFO, String.format(PROGRESS_FORMAT, getName(), status, progressText));
        }
    }

    protected void log(String message) {
        Logger logger = LoginSecurity.getInstance().getLogger();
        logger.log(Level.INFO, "Migrating " + getName() + ": " + message);
    }

    protected void copyFile(File from, File to) throws IOException {
        copyFile(new FileInputStream(from), to);
    }

    protected void copyFile(InputStream from, File to) throws IOException {
        FileOutputStream output = null;
        to.getParentFile().mkdirs();
        try {
            output = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int length;
            while((length = from.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } finally {
            try {
                from.close();
            } catch(IOException e) {
            }
            if(output != null) {
                try {
                    output.close();
                } catch(IOException e) {
                }
            }
        }
    }
}
