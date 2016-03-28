package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.commands.CommandLogin;
import com.lenis0012.bukkit.loginsecurity.commands.CommandRegister;
import com.lenis0012.pluginutils.Module;

import java.util.logging.Level;

public class GeneralModule extends Module<LoginSecurity> {
    public GeneralModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        registerCommands();
        registerListeners();
    }

    @Override
    public void disable() {
    }

    private void registerCommands() {
        logger().log(Level.INFO, "Registering commands...");
        register(new CommandLogin(plugin), "login");
        register(new CommandRegister(), "register");
    }

    private void registerListeners() {
        logger().log(Level.INFO, "Registering listeners...");
        register(new PlayerListener());
    }
}
