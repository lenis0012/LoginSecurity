package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.pluginutils.PluginHolder;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LegacyMigration extends Migration {

    @Override
    public boolean executeAutomatically() {
        final PluginHolder plugin = LoginSecurity.getInstance();
        final File file = new File(plugin.getDataFolder(), "users.db");
        return file.exists();
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public boolean execute() {
        final PluginHolder plugin = LoginSecurity.getInstance();
        final File file = new File(plugin.getDataFolder(), "users.db");
        final Logger logger = plugin.getLogger();

        final String url = "jdbc:sqlite:" + file.getPath();
        final String driver = "org.sqlite.JDBC";

        // Load driver
        log("Loading driver...");
        try {
            Class.forName(driver);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to load SQLite driver.");
            return false;
        }

        // Load SQL Query
        log("Loading upgrade query...");
        final StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            final InputStream input = plugin.getResource("sql/sqlite/legacy_upgrade.sql");
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

        log("Creating backup...");
        try {
            copyFile(file, new File(plugin.getDataFolder(), "users.backup.db"));
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Failed to create backup", e);
        }

        log("Executing query, this might take a while...");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
//            statement.setQueryTimeout(120);
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

        return true;
    }

    @Override
    public String getName() {
        return "Legacy";
    }
}
