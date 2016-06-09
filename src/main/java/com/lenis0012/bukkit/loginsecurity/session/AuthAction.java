package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.InventorySerializer;
import org.bukkit.entity.Player;

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
    protected boolean rehabPlayer(final PlayerSession session) {
        final Player player = session.getPlayer();
        final PlayerProfile profile = session.getProfile();
        boolean changed = false;

        if(profile.getInventory() != null) {
            InventorySerializer.deserializeInventory(profile.getInventory(), player.getInventory());
//            player.updateInventory();
            profile.setInventory(null);
            changed = true;
        }

        if(profile.getLoginLocation() != null) {
            player.teleport(profile.getLoginLocation().asLocation());
            profile.setLoginLocation(null);
            changed = true;
        }

        return changed;
    }
}
