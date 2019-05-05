package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.database.datasource.SingleConnectionDataSource;
import com.lenis0012.bukkit.loginsecurity.database.datasource.sqlite.SQLiteConnectionPoolDataSource;
import com.lenis0012.pluginutils.Module;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.bukkit.Bukkit;

import javax.sql.ConnectionPoolDataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;

public class NewStorageModule extends Module<LoginSecurity> {
    private SingleConnectionDataSource dataSource;
    private LoginSecurityDatabase database;

    public NewStorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        String platform;
        ConnectionPoolDataSource dataSourceConfig;
        final File configFile = new File(plugin.getDataFolder(), "database.yml");
        if(!configFile.exists()) copyFile(plugin.getResource("database.yml"), configFile);
        final Configuration config = new Configuration(configFile);
        if(config.getBoolean("mysql.enabled")) {
            platform = "mysql";
            dataSourceConfig = createMysqlDataSource(config);
        } else {
            platform = "sqlite";
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

    private ConnectionPoolDataSource createMysqlDataSource(Configuration config) {
        MysqlConnectionPoolDataSource mysqlConfig = new MysqlConnectionPoolDataSource();
        mysqlConfig.setUrl("jdbc:mysql://" + config.getString("mysql.host") + "/" + config.getString("mysql.database"));
        mysqlConfig.setUser(config.getString("mysql.username"));
        mysqlConfig.setPassword(config.getString("mysql.password"));
        return mysqlConfig;
    }

    private ConnectionPoolDataSource createSqliteDataSource() {
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
