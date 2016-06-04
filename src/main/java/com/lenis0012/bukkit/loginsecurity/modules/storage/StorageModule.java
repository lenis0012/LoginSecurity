package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.bukkit.loginsecurity.storage.Migration;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class StorageModule extends Module<LoginSecurity> {
    private SessionManager sessionManager;
    private EbeanServer database;

    public StorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        this.sessionManager = new SessionManager();

        // Load config
        File file = new File(plugin.getDataFolder(), "database.yml");
        if(!file.exists()) {
            copyFile(plugin.getResource("/database.yml"), file);
        }
        Configuration config = new Configuration(file);
        config.reload(false);

        // Server settings
        ServerConfig server = new ServerConfig();
        server.setDefaultServer(false);
        server.setRegister(false);
        server.setClasses(plugin.getDatabaseClasses());
        server.setName("LoginSecurity");

        // Datasource settings
        DataSourceConfig source = new DataSourceConfig();
        final int isolation = TransactionIsolation.getLevel(config.getString("isolation"));
        final boolean mysql = config.getBoolean("mysql.enabled");
        source.setDriver(mysql ? "com.mysql.jdbc.Driver" : "org.sqlite.JDBC");
        source.setIsolationLevel(isolation);
        if(mysql) {
            source.setUrl(String.format("jdbc:mysql://%s/%s", config.getString("mysql.host"), config.getString("mysql.database")));
            source.setUsername(config.getString("username"));
            source.setPassword(config.getString("password"));
        } else {
            server.setDatabasePlatform(new SQLitePlatform());
            server.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
            String path = plugin.getDataFolder().getPath().replaceAll("\\\\", "/");
            source.setUrl("jdbc:sqlite:" + path + "/LoginSecurity.db");
        }
        server.setDataSourceConfig(source);

        // Create server
        plugin.getLogger().log(Level.INFO, "Connection to database....");
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        this.database = EbeanServerFactory.create(server);
        Thread.currentThread().setContextClassLoader(previous);

        // List migrations
        List<String> migrations = Lists.newArrayList();
        try {
            JarFile jarFile = new JarFile(getPluginFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().startsWith("sqlite/") && entry.getName().contains("__")) {
                    migrations.add(entry.getName());
                }
            }
        } catch(IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to scan migration scripts!");
        }

        // Apply missing migrations
        plugin.getLogger().log(Level.INFO, "Checking database version...");
        Collections.sort(migrations);
        boolean installed = isInstalled();
        String platform = mysql ? "mysql" : "sqlite";
        SpiEbeanServer ebean = (SpiEbeanServer) database;
        DdlGenerator generator = ebean.getDdlGenerator();
        for(String migration : migrations) {
            String[] parts = migration.split(Pattern.quote("__"));
            String version = parts[0];
            String name = parts[1].replace("_", " ");
            name = name.substring(0, name.length() - ".sql".length()); // Remove extension
            if(!installed || database.find(Migration.class).where().ieq("version", version).findRowCount() == 0) {
                plugin.getLogger().log(Level.INFO, "Applying database upgrade " + version + ": " + name);
                String content = getContent("/" + platform + "/" + migration);
                generator.runScript(false, content);
            }
        }
        plugin.getLogger().log(Level.INFO, "Database loaded successfully!");
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

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
