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

package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.LogLevel;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.Migration;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.ProfileUtil;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class StorageModule extends Module<LoginSecurity> implements Comparator<String> {
    private EbeanServer database;
    private boolean mysql;
    private List<String> migrations;

    public StorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // Load config
        File file = new File(plugin.getDataFolder(), "database.yml");
        if(!file.exists()) {
            copyFile(plugin.getResource("database.yml"), file);
        }
        Configuration config = new Configuration(file);
        config.reload(true);

        // Check config for mysql settings
        FileConfiguration conf = plugin.getConfig();
        if(conf.contains("MySQL")) {
            // Rewrite mysql settings
            config.set("mysql.enabled", conf.getBoolean("MySQL.use"));
            String host = conf.getString("MySQL.host", "localhost") + ":" + conf.getInt("MySQL.port", 3306);
            config.set("mysql.host", host);
            config.set("mysql.username", conf.getString("MySQL.username"));
            config.set("mysql.password", config.getString("MySQL.password"));
            config.set("mysql.database", config.getString("MySQL.database"));
            config.save();

            // Cleanup old config
            conf.set("MySQL", null);
            conf.set("settings", null);
            plugin.saveConfig();
        }

        // Server settings
        ServerConfig server = new ServerConfig();
        server.setDefaultServer(false);
        server.setRegister(false);
        server.setClasses(plugin.getDatabaseClasses());
        server.setName("LoginSecurityDB");
        server.setLoggingLevel(LogLevel.NONE);
        server.setEnhanceLogLevel(0);

        // Datasource settings
        DataSourceConfig source = new DataSourceConfig();
        final int isolation = TransactionIsolation.getLevel(config.getString("isolation"));
        this.mysql = config.getBoolean("mysql.enabled");
        source.setDriver(mysql ? "com.mysql.jdbc.Driver" : "org.sqlite.JDBC");
        source.setIsolationLevel(isolation);
        source.setHeartbeatSql("select 1");
        if(mysql) {
            source.setUrl(String.format("jdbc:mysql://%s/%s", config.getString("mysql.host"), config.getString("mysql.database")));
            source.setUsername(config.getString("mysql.username"));
            source.setPassword(config.getString("mysql.password"));
            System.out.println("MYSQL");
        } else {
            server.setDatabasePlatform(new SQLitePlatform());
            server.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
            String path = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
            source.setUrl("jdbc:sqlite:" + path + "/LoginSecurity.db");
            source.setUsername("trump");
            source.setPassword("donald");
        }
        server.setDataSourceConfig(source);

        // Create server
        plugin.getLogger().log(Level.INFO, "Connection to database....");
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        this.database = EbeanServerFactory.create(server);
        database.getAdminLogging().setLogLevel(LogLevel.NONE);
        Thread.currentThread().setContextClassLoader(previous);

        // List migrations
        this.migrations = Lists.newArrayList();
        try {
            JarFile jarFile = new JarFile(getPluginFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().startsWith("sql/sqlite/") && entry.getName().contains("__")) {
                    migrations.add(entry.getName().substring("sql/sqlite/".length()));
                }
            }
        } catch(IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to scan migration scripts!");
        }
        Collections.sort(migrations, this);

        // Apply missing migrations
        applyMissingUpgrades();
    }

    /**
     * Apply any missing database upgrades.
     * This normally runs on startup.
     *
     * It is also recommended to run when the database is modified and might be on an earlier version.
     * Ex. Legacy migrations upgrade database to v1.
     */
    public void applyMissingUpgrades() {
        plugin.getLogger().log(Level.INFO, "Checking database version...");
        boolean installed = isInstalled();
        String platform = mysql ? "mysql" : "sqlite";
        SpiEbeanServer ebean = (SpiEbeanServer) database;
        DdlGenerator generator = ebean.getDdlGenerator();
        int updatesRan = 0;
        for(String migration : migrations) {
            String[] parts = migration.split(Pattern.quote("__"));
            String version = parts[0];
            String name = parts[1].replace("_", " ");
            name = name.substring(0, name.length() - ".sql".length()); // Remove extension
            if(!installed || database.find(Migration.class).where().ieq("version", version).findRowCount() == 0) {
                plugin.getLogger().log(Level.INFO, "Applying database upgrade " + version + ": " + name);
                String content = getContent("sql/" + platform + "/" + migration);
                if(!content.isEmpty()) {
                    generator.runScript(false, content);
                }
                database.save(new Migration(version, name, new Timestamp(System.currentTimeMillis())));
                updatesRan++;
            }
        }
        plugin.getLogger().log(Level.INFO, "Applied " + updatesRan + " missing database upgrades.");

        // Fix profile uuids
        List<PlayerProfile> profiles = database.find(PlayerProfile.class).where().isNull("uuid_mode").findList();
        profiles.addAll(database.find(PlayerProfile.class).where().eq("uuid_mode", UserIdMode.UNKNOWN).findList());
        if(!profiles.isEmpty()) {
            plugin.getLogger().log(Level.INFO, "Refactoring UUID for " + profiles.size() + " profiles...");
            UserIdMode mode = ProfileUtil.getUserIdMode();
            for(PlayerProfile profile : profiles) {
                profile.setUniqueUserId(mode.getUserId(profile));
                profile.setUniqueIdMode(mode);
            }
            database.save(profiles);
            plugin.getLogger().log(Level.INFO, "Successfully updated UUIDs!");
        }
    }

    public EbeanServer getDatabase() {
        return database;
    }

    public boolean isRunningMySQL() {
        return mysql;
    }

    @Override
    public void disable() {
    }

    private String getContent(String resource) {
        try {
            InputStream input = plugin.getResource(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            return builder.toString();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't read resource content", e);
        }
    }

    private boolean isInstalled() {
        try {
            database.find(Migration.class).findRowCount();
            return true;
        } catch(PersistenceException e) {
            return false;
        }
    }

    private File getPluginFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(plugin);
        } catch(Exception e) {
            throw new RuntimeException("Couldn't get context class loader", e);
        }
    }

    private ClassLoader getClassLoader() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getClassLoader");
            method.setAccessible(true);
            return (ClassLoader) method.invoke(plugin);
        } catch(Exception e) {
            throw new RuntimeException("Couldn't get context class loader", e);
        }
    }

    private void copyFile(InputStream from, File to) {
        try {
            FileOutputStream output = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int length;
            while((length = from.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.close();
            from.close();
        } catch(IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to copy resource", e);
        }
    }

    @Override
    public int compare(String o1, String o2) {
        int i0 = Integer.valueOf(o1.split(Pattern.quote("__"))[0]);
        int i1 = Integer.valueOf(o2.split(Pattern.quote("__"))[0]);
        return Integer.compare(i0, i1);
    }
}
