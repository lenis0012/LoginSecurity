package com.lenis0012.bukkit.loginsecurity;

import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;

public class LoginSecurity extends PluginHolder {
    private LoginSecurityConfig config;

    public LoginSecurity() {
        super(ConfigurationModule.class);
    }

    @Override
    public void enable() {
        // Load config
        ConfigurationModule module = getModule(ConfigurationModule.class);
        this.config = module.createCustomConfig(LoginSecurityConfig.class);
        config.reload();
        config.save();
    }

    @Override
    public void disable() {
    }

    public LoginSecurityConfig config() {
        return config;
    }
}
