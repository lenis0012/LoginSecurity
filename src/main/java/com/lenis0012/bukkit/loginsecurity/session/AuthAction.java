/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lenis0012.bukkit.loginsecurity.session;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.exceptions.ProfileRefreshException;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.InventorySerializer;
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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LoginSecurity.getInstance().getDatabase().save(model);
            }
        };
        LoginSecurity.getExecutorService().execute(runnable);
    }

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

        player.removePotionEffect(PotionEffectType.BLINDNESS);
        if(profile.getInventory() != null) {
            InventorySerializer.deserializeInventory(profile.getInventory(), player.getInventory());
            profile.setInventory(null);
            changed = true;
        }

        if(profile.getLoginLocation() != null) {
            Location location = profile.getLoginLocation().asLocation();
            if(location != null) {
                player.teleport(location);
            }
            profile.setLoginLocation(null);
            changed = true;
        }

        return changed;
    }
}
