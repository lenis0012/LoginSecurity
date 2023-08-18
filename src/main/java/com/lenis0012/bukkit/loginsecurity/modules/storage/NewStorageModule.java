package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.database.datasource.SingleConnectionDataSource;
import com.lenis0012.bukkit.loginsecurity.database.datasource.sqlite.SQLiteConnectionPoolDataSource;
import com.lenis0012.bukkit.loginsecurity.util.ReflectionBuilder;
import com.lenis0012.pluginutils.config.CommentConfiguration;
import com.lenis0012.pluginutils.modules.Module;
import lombok.Getter;
import org.bukkit.Bukkit;

import javax.sql.ConnectionPoolDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        final CommentConfiguration config = new CommentConfiguration(configFile);
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
            plugin.getLogger().log(Level.SEVERE, "Failed to initiate database", e);
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

    ConnectionPoolDataSource createMysqlDataSource(CommentConfiguration config) {
        if (ReflectionBuilder.classExists("com.mysql.cj.jdbc.MysqlConnectionPoolDataSource")) {
            return createMysqlDataSourceCJ(config);
        } else if(ReflectionBuilder.classExists("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource")) {
            return createMysqlDataSourceOld(config);
        }
        throw new IllegalStateException("Failed to create MySQL data source (no compatible driver found)");
    }

    private ConnectionPoolDataSource createMysqlDataSourceCJ(CommentConfiguration config) {
        return new ReflectionBuilder("com.mysql.cj.jdbc.MysqlConnectionPoolDataSource")
            .call("setUrl", "jdbc:mysql://" + config.getString("mysql.host") + "/" + config.getString("mysql.database"))
            .call("setUser", config.getString("mysql.username"))
            .call("setPassword", config.getString("mysql.password"))
            .build(ConnectionPoolDataSource.class);
    }

    private ConnectionPoolDataSource createMysqlDataSourceOld(CommentConfiguration config) {
        return new ReflectionBuilder("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource")
            .call("setUrl", "jdbc:mysql://" + config.getString("mysql.host") + "/" + config.getString("mysql.database"))
            .call("setUser", config.getString("mysql.username"))
            .call("setPassword", config.getString("mysql.password"))
            .build(ConnectionPoolDataSource.class);
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
