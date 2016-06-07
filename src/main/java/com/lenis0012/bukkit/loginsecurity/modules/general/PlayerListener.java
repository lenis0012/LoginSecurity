package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * PlayerListener.
 *
 * Handles player management.
 * e.x Prevent players from moving when not logged in.
 */
public class PlayerListener implements Listener {
    private final List<String> ALLOWED_COMMANDS = Lists.newArrayList("/login ", "/register ");
    private final GeneralModule general;

    public PlayerListener(GeneralModule general) {
        this.general = general;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // Pre-load player to improve performance...
        LoginSecurity.getSessionManager().preloadSession(event.getName(), event.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Unload player
        LoginSecurity.getSessionManager().onPlayerLogout(event.getPlayer());
        MetaData.unset(event.getPlayer(), "ls_login_tries");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final AuthMode authMode = session.getAuthMode();
        session.getProfile().setLastLogin(new Timestamp(System.currentTimeMillis()));

        // Message
        if(authMode.hasAuthMessage()) {
            player.sendMessage(ChatColor.RED + authMode.getAuthMessage());
        }

        if(session.isAuthorized()) {
            return;
        }

        final Location origin = player.getLocation().clone();
        switch(general.getLocationMode()) {
            case SPAWN:
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                session.getProfile().setLoginLocation(new PlayerLocation(origin));
                session.saveProfileAsync();
                break;
            case RANDOM:
            case DEFAULT:
                return; // Do nothing (for now)
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    /**
     * Player action filtering.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPrepcoress(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Check whitelisted commands
        final String message = event.getMessage().toLowerCase();
        for(String cmd : ALLOWED_COMMANDS) {
            if(message.startsWith(cmd)) {
                return;
            }
        }

        if(message.startsWith("/f")) {
            event.setMessage("/LOGIN_SECURITY_FACTION_REPLACEMENT_FIX");
        }

        // Cancel
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        // Prevent moving
        event.setTo(event.getFrom());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        final Player player = (Player) event.getEntity();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return; // Not a player
        final Player player = (Player) event.getEntity();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTarget(EntityTargetEvent event) {
        if(!(event.getTarget() instanceof Player)) return; // Not a player
        final Player player = (Player) event.getTarget();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isLoggedIn()) return;

        event.setCancelled(true);
    }
}
