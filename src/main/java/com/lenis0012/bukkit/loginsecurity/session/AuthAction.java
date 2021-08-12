package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerInventory;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.InventorySerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.logging.Level;

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
        if(profile.getInventoryId() != null) {
            try {
                final PlayerInventory serializedInventory = LoginSecurity.getDatastore().getInventoryRepository()
                        .findByIdBlocking(profile.getInventoryId());
                if(serializedInventory != null) {
                    Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> {
                        InventorySerializer.deserializeInventory(serializedInventory, player.getInventory());
                        profile.setInventoryId(null);
                        session.saveProfileAsync();
                        // TODO: Delete inventory
                    });
                } else {
                    LoginSecurity.getInstance().getLogger().log(Level.WARNING, "Couldn't find player's inventory");
                    profile.setInventoryId(null);
                    session.saveProfileAsync();
                }
            } catch (SQLException e) {
                LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to load player inventory", e);
            }
        }

        if(profile.getLoginLocationId() != null) {
            try {
                final PlayerLocation serializedLocation = LoginSecurity.getDatastore().getLocationRepository()
                        .findByIdBlocking(profile.getLoginLocationId());
                Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> {
                    player.teleport(serializedLocation.asLocation());
                    profile.setLoginLocationId(null);
                    session.saveProfileAsync();
                    // TODO: Delete location
                });
                if(serializedLocation != null) {
                    LoginSecurity.getInstance().getLogger().log(Level.WARNING, "Couldn't find player's login location");
                    profile.setLoginLocationId(null);
                    session.saveProfileAsync();
                }
            } catch (SQLException e) {
                LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to load player login location", e);
            }
        }

        if(LoginSecurity.getConfiguration().isHideInventory()) {
            Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), player::updateInventory);
        }

//        if(profile.getLoginLocation() != null) {
//            final PlayerLocation loginLocation = profile.getLoginLocation();
//            loginLocation.getWorld(); // hotfix: Populate method
//            final Location location = loginLocation.asLocation();
//            if(location != null) {
//                Bukkit.getScheduler().runTask(LoginSecurity.getInstance(), () -> player.teleport(location));
//            }
//            profile.setLoginLocation(null);
//        }
    }
}
