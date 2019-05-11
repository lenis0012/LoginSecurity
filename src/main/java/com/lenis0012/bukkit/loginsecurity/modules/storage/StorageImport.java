package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import org.bukkit.command.CommandSender;

public interface StorageImport extends Runnable {

    boolean isPossible();

    static StorageImport fromSourceName(String sourceName, CommandSender sender) {
        switch(sourceName.toLowerCase()) {
            case "loginsecurity":
                return new LoginSecurityImport((LoginSecurity) LoginSecurity.getInstance(), sender);
            default:
                return null;
        }
    }
}
