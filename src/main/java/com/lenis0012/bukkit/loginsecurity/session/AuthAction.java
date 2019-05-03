package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.InventorySerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public abstract class AuthAction {
    private final AuthActionType type;
    private final AuthService service;
    private final Object serviceProvider;

    public <T> AuthAction(AuthActionType type, AuthService<T> service, T serviceProvider) {
        this.type = type;
        this.service = service;
        this.serviceProvider = serviceProvider;
    }

    public AuthActionType getType() {
        return type;
    }

    public AuthService getService() {
        return service;
    }

    protected Object getServiceProvider() {
        return serviceProvider;
    }

    public abstract AuthMode run(PlayerSession session, ActionResponse response);

    protected void save(final Object model) {
        Runnable runnable = () -> LoginSecurity.getDatabase().save(model);
        LoginSecurity.getExecutorService().execute(runnable);
    }

    /**
     * Return player to their original state.
     *
     * @param session Session of the player
     * @return True if profile changed, false otherwise
     */
    protected void rehabPlayer(final PlayerSession session) {
        final Player player = session.getPlayer();
        final PlayerProfile profile = session.getProfile();

        Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> player.removePotionEffect(PotionEffectType.BLINDNESS));
        if(profile.getInventory() != null) {
            InventorySerializer.deserializeInventory(profile.getInventory(), player.getInventory());
            profile.setInventory(null);
        }

        if(profile.getLoginLocation() != null) {
            final PlayerLocation loginLocation = profile.getLoginLocation();
            loginLocation.getWorld(); // hotfix: Populate method
            final Location location = loginLocation.asLocation();
            if(location != null) {
                Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> player.teleport(location));
            }
            profile.setLoginLocation(null);
        }
    }
}
