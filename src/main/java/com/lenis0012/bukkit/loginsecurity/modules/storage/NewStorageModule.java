package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.database.datasource.SingleConnectionDataSource;
import com.lenis0012.bukkit.loginsecurity.database.datasource.sqlite.SQLiteConnectionPoolDataSource;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;

import javax.sql.ConnectionPoolDataSource;
import java.io.*;
import java.sql.SQLException;
import java.util.logging.Level;

public class NewStorageModule extends Module<LoginSecurity> {
    private SingleConnectionDataSource dataSource;
    private LoginSecurityDatabase database;
    @Getter
    private String platform;

    public NewStorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // Create backup
        final File configFile = new File(plugin.getDataFolder(), "database.yml");
        if(!configFile.exists()) copyFile(plugin.getResource("database.yml"), configFile);
        final Configuration config = new Configuration(configFile);
        config.reload();

        ConnectionPoolDataSource dataSourceConfig;
        if(config.getBoolean("mysql.enabled")) {
            this.platform = "mysql";
            dataSourceConfig = createMysqlDataSource(config);
        } else {
            this.platform = "sqlite";
            dataSourceConfig = createSqliteDataSource();
        }

        this.dataSource = new SingleConnectionDataSource(plugin, dataSourceConfig);
        this.database = new LoginSecurityDatabase(plugin, dataSource);
        try {
            dataSource.createConnection();
            new MigrationRunner(plugin, dataSource, platform).run();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initiate database");
        }
    }

    @Override
    public void disable() {
        try {
            dataSource.shutdown();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to shut down database", e);
        }
    }

    ConnectionPoolDataSource createMysqlDataSource(Configuration config) {
        MysqlConnectionPoolDataSource mysqlConfig = new MysqlConnectionPoolDataSource();
        mysqlConfig.setUrl("jdbc:mysql://" + config.getString("mysql.host") + "/" + config.getString("mysql.database"));
        mysqlConfig.setUser(config.getString("mysql.username"));
        mysqlConfig.setPassword(config.getString("mysql.password"));
        return mysqlConfig;
    }

    ConnectionPoolDataSource createSqliteDataSource() {
        File backupFile = new File(plugin.getDataFolder(), "LoginSecurity.db.3.0.backup");
        if(!backupFile.exists()) {
            try {
                copyFile(new FileInputStream(new File(plugin.getDataFolder(), "LoginSecurity.db")), backupFile);
            } catch (FileNotFoundException e) { }
        }

        SQLiteConnectionPoolDataSource sqliteConfig = new SQLiteConnectionPoolDataSource();
        final String path = new File(plugin.getDataFolder(), "LoginSecurity.db").getPath();
        sqliteConfig.setUrl("jdbc:sqlite:" + path);
        return sqliteConfig;
    }

    public LoginSecurityDatabase getDatabase() {
        return database;
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
}
