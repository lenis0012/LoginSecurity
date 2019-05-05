package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.database.datasource.SingleConnectionDataSource;
import com.lenis0012.bukkit.loginsecurity.database.datasource.sqlite.SQLiteConnectionPoolDataSource;
import com.lenis0012.pluginutils.Module;
import org.bukkit.Bukkit;

import java.io.File;
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
        SQLiteConnectionPoolDataSource sqliteConfig = new SQLiteConnectionPoolDataSource();
        final String path = new File(plugin.getDataFolder(), "LoginSecurity.db").getPath();
        sqliteConfig.setUrl("jdbc:sqlite:" + path);

        this.dataSource = new SingleConnectionDataSource(plugin, sqliteConfig);
        this.database = new LoginSecurityDatabase(plugin, dataSource);
        try {
            dataSource.createConnection();
            new MigrationRunner(plugin, dataSource, "sqlite").run();
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

    public LoginSecurityDatabase getDatabase() {
        return database;
    }
}
