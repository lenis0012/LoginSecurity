package com.lenis0012.bukkit.loginsecurity.integrate.autoin;

import com.gmail.bartlomiejkmazur.autoin.api.APICore;
import com.gmail.bartlomiejkmazur.autoin.api.auth.BukkitLoginPlugin;
import com.gmail.bartlomiejkmazur.autoin.api.auth.EventPriority;
import com.gmail.bartlomiejkmazur.autoin.core.server.bukkit.BukkitAutoIn;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.LoginAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LoginPluginLoginSecurity extends BukkitLoginPlugin {
    private final LoginSecurity plugin;
    private final BukkitAutoIn autoIn;

    public LoginPluginLoginSecurity(LoginSecurity plugin) {
        this.plugin = plugin;
        this.autoIn = (BukkitAutoIn) Bukkit.getPluginManager().getPlugin("AutoIn");
    }

    public void register() {
        APICore.getAPI().addLoginPlugin("LoginSecurity", this);
    }

    @Override
    public void forceLogin(Object o) {
        final Player player = (Player) o;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        session.performAction(new LoginAction(AuthService.PLUGIN, autoIn));
    }

    @Override
    public void forceRegister(String s) {
    }

    @Override
    public void forceLogout(Object o) {
        // TODO: Logout
    }

    @Override
    public boolean needRegisterToLogin(String s) {
        return false;
    }

    @Override
    public boolean isLoggedIn(Object o) {
        final Player player = (Player) o;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        return session.isAuthorized();
    }

    @Override
    public boolean isRegistered(String nick) {
        final Player player = Bukkit.getPlayer(nick);
        if(player != null) {
            final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
            return session.getAuthMode() != AuthMode.UNREGISTERED;
        }
        // TODO: Support offline player...
        return false;
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public EventPriority getJoinEventPriority() {
        return EventPriority.MONITOR;
    }
}
