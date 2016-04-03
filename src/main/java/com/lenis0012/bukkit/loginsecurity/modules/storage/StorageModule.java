package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.pluginutils.Module;

public class StorageModule extends Module<LoginSecurity> {
    private SessionManager sessionManager;

    public StorageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        this.sessionManager = new SessionManager();
    }

    @Override
    public void disable() {
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
