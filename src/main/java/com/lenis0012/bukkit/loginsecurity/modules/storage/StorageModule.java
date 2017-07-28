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

import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.jdbc.JdbcDaoFactory;
import com.lenis0012.bukkit.loginsecurity.database.jdbc.platform.JdbcPlatform;
import com.lenis0012.bukkit.loginsecurity.database.jdbc.platform.MysqlPlatform;
import com.lenis0012.bukkit.loginsecurity.database.jdbc.platform.SqlitePlatform;
import com.lenis0012.bukkit.loginsecurity.storage.Migration;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StorageModule extends Module<LoginSecurity> implements Comparator<String> {
    private boolean mysql;
    private List<String> migrations;

    public StorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // Load config
        File file = new File(plugin.getDataFolder(), "database-settings.yml");
        if(!file.exists()) {
            copyFile(plugin.getResource("database.yml"), file);
        }
        Configuration config = new Configuration(file);
        config.reload(true);

        // Check config for legacy settings
        new LegacySettings(plugin).convert(config);

        // Select platform and create DAO factory
        JdbcPlatform platform = getJdbcPlatform(config);
        ConfigurationSection section = config.getConfigurationSection("configuration." + config.getString("platform", "sqlite"));
        JdbcDaoFactory daoFactory = JdbcDaoFactory.build(logger(), section, platform);

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
        migrations.sort(this);

        // Apply missing migrations
        applyMissingUpgrades(daoFactory);
    }

    /**
     * Apply any missing database upgrades.
     * This normally runs on startup.
     *
     * It is also recommended to run when the database is modified and might be on an earlier version.
     * Ex. Legacy migrations upgrade database to v1.
     */
    public void applyMissingUpgrades(JdbcDaoFactory daoFactory) {
        plugin.getLogger().log(Level.INFO, "Checking database version...");
        List<String> installedMigrations = daoFactory.getMigrationDao().findAll().stream().map(Migration::getVersion).collect(Collectors.toList());

        int updatesRan = 0;
        for(String migration : migrations) {
            String[] parts = migration.split(Pattern.quote("__"));
            String version = parts[0];
            String name = parts[1].replace("_", " ");
            name = name.substring(0, name.length() - ".sql".length()); // Remove extension
            if(!installedMigrations.contains(version)) {
                plugin.getLogger().log(Level.INFO, "Applying database upgrade " + version + ": " + name);
                String content = getContent("sql/" + daoFactory.getPlatformName() + "/" + migration);
                if(!content.isEmpty()) {
                    daoFactory.runSql(content);
                }
                daoFactory.getMigrationDao().insertMigration(new Migration(version, name, new Timestamp(System.currentTimeMillis())));
                updatesRan++;
            }
        }
        plugin.getLogger().log(Level.INFO, "Applied " + updatesRan + " missing database upgrades.");

        // Fix profile uuids
//        List<PlayerProfile> profiles = database.find(PlayerProfile.class).where().isNull("uuid_mode").findList();
//        profiles.addAll(database.find(PlayerProfile.class).where().eq("uuid_mode", UserIdMode.UNKNOWN).findList());
//        if(!profiles.isEmpty()) {
//            plugin.getLogger().log(Level.INFO, "Refactoring UUID for " + profiles.size() + " profiles...");
//            UserIdMode mode = ProfileUtil.getUserIdMode();
//            for(PlayerProfile profile : profiles) {
//                profile.setUniqueUserId(mode.getUserId(profile));
//                profile.setUniqueIdMode(mode);
//            }
//            database.save(profiles);
//            plugin.getLogger().log(Level.INFO, "Successfully updated UUIDs!");
//        }
    }

    @Override
    public void disable() {
    }

    private JdbcPlatform getJdbcPlatform(Configuration configuration) {
        switch(configuration.getString("platform", "sqlite").toLowerCase()) {
            case "sqlite":
                logger().log(Level.INFO, "Selecting database platform: sqlite");
                return new SqlitePlatform();
            case "mysql":
                logger().log(Level.INFO, "Selecting database platform: mysql");
                return new MysqlPlatform();
            default:
                logger().log(Level.WARNING, "Unknown database platform \"" +
                        configuration.getString("platform") + "\", defaulting to sqlite.");
                configuration.set("platform", "sqlite");
                configuration.save();
                return new SqlitePlatform();
        }
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
