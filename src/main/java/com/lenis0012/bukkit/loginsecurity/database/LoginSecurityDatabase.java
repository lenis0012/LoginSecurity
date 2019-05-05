package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import lombok.Getter;

import javax.sql.DataSource;

@Getter
public class LoginSecurityDatabase {
    private final ProfileRepository profileRepository;
    private final InventoryRepository inventoryRepository;

    public LoginSecurityDatabase(LoginSecurity plugin, DataSource dataSource) {
        this.profileRepository = new ProfileRepository(plugin, dataSource);
        this.inventoryRepository = new InventoryRepository(plugin, dataSource);
    }
}
