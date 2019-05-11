package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.database.InventoryRepository;
import com.lenis0012.bukkit.loginsecurity.database.LocationRepository;
import com.lenis0012.bukkit.loginsecurity.database.LoginSecurityDatabase;
import com.lenis0012.bukkit.loginsecurity.database.ProfileRepository;
import com.lenis0012.bukkit.loginsecurity.database.datasource.SingleConnectionDataSource;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.sql.SQLException;

public class LoginSecurityImport implements StorageImport {
    private final LoginSecurity loginSecurity;
    private final CommandSender sender;

    public LoginSecurityImport(LoginSecurity loginSecurity, CommandSender sender) {
        this.loginSecurity = loginSecurity;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            message("Initializing secondary import database.");
            final NewStorageModule storageModule = loginSecurity.getModule(NewStorageModule.class);
            SingleConnectionDataSource secondaryDataSource = createDataSource(storageModule);

            final LoginSecurityDatabase datastore = LoginSecurity.getDatastore();
            final ProfileRepository profiles = new ProfileRepository(loginSecurity, secondaryDataSource);
            final LocationRepository locations = new LocationRepository(loginSecurity, secondaryDataSource);
            final InventoryRepository inventories = new InventoryRepository(loginSecurity, secondaryDataSource);

            message("Importing inventories.");
            datastore.getInventoryRepository().batchInsert(inventories::iterateAllBlocking);

            message("Importing locations.");
            datastore.getLocationRepository().batchInsert(locations::iterateAllBlocking);

            message("Importing profiles.");
            datastore.getProfileRepository().batchInsert(profiles::iterateAllBlocking);

            message("Done, imported all profiles.");
        } catch (SQLException e) {
            message("Failed to import profiles: " + e.getMessage());
        }
    }

    @Override
    public boolean isPossible() {
        final NewStorageModule storageModule = loginSecurity.getModule(NewStorageModule.class);
        if(storageModule.getPlatform().equalsIgnoreCase("mysql")) {
            return new File(loginSecurity.getDataFolder(), "LoginSecurity.db").exists();
        } else {
            return true; // Wont verify is mysql is valid
        }
    }

    private void message(String message) {
        Bukkit.getScheduler().runTask(loginSecurity, () -> sender.sendMessage(message));
    }

    private SingleConnectionDataSource createDataSource(NewStorageModule storageModule) {
        if(storageModule.getPlatform().equalsIgnoreCase("mysql")) {
            return new SingleConnectionDataSource(loginSecurity, storageModule.createSqliteDataSource());
        } else {
            final Configuration configuration = new Configuration(new File(loginSecurity.getDataFolder(), "database.yml"));
            configuration.reload();
            return new SingleConnectionDataSource(loginSecurity, storageModule.createMysqlDataSource(configuration));
        }
    }
}
