package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.pluginutils.PluginHolder;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
        final StorageModule storage = plugin.getModule(StorageModule.class);
        final File file = new File(plugin.getDataFolder(), "users.db");
        final Logger logger = plugin.getLogger();
        final String platform = storage.isRunningMySQL() ? "mysql" : "sqlite";

        String url = "jdbc:sqlite:" + file.getPath();
        String driver = "org.sqlite.JDBC";

        // Load driver
        log("Loading driver...");
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to load SQL driver.");
            return false;
        }

        // Load SQL Query
        log("Loading upgrade query...");
        final StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            final InputStream input = plugin.getResource("sql/" + platform + "/legacy_upgrade.sql");
            reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Failed to read update query", e);
            return false;
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {}
            }
        }

        // MySQL: Apply upgrade script
        if(storage.isRunningMySQL()) {
            SqlUpdate update = plugin.getDatabase().createSqlUpdate(builder.toString());
            update.execute();
        }

        // SQLite: Apply upgrade script
        if(!storage.isRunningMySQL()) {
            // Create backup
            log("Creating backup...");
            try {
                copyFile(file, new File(plugin.getDataFolder(), "users.backup.db"));
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Failed to create backup", e);
            }

            // Execute upgrade query
            log("Executing query, this might take a while...");
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement();
                statement.execute(builder.toString());
            } catch(SQLException e) {
                logger.log(Level.SEVERE, "Failed to run SQL query", e);
                return false;
            } finally {
                if(connection != null) {
                    try {
                        connection.close();
                    } catch(SQLException e) {}
                }
            }

            // Update files
            log("Finalizing...");
            File newFile = new File(plugin.getDataFolder(), "LoginSecurity.sql");
            if(newFile.exists()) {
                log("Deleting old database...");
                long startTime = System.currentTimeMillis();
                while(!newFile.delete()) {
                    if(startTime + 30000L > System.currentTimeMillis()) {
                        logger.log(Level.WARNING, "Failed to delete old database, please follow instructions on wiki to resolve!");
                        return false;
                    }
                    try {
                        Thread.sleep(50L);
                    } catch(InterruptedException e) {
                    }
                }
            }
            log("Renaming new database...");
            long startTime = System.currentTimeMillis();
            while(!file.renameTo(newFile)) {
                if(startTime + 30000L > System.currentTimeMillis()) {
                    logger.log(Level.WARNING, "Failed to rename new database, please follow instructions on wiki to resolve!");
                    return false;
                }
                try {
                    Thread.sleep(50L);
                } catch(InterruptedException e) {
                }
            }
        }

        storage.applyMissingUpgrades();
        return true;
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
