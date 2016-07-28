package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.InventorySerializer;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

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
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getName().equalsIgnoreCase(event.getName())) {
                PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
                if(session.isAuthorized()) {
                    event.setLoginResult(Result.KICK_OTHER);
                    event.setKickMessage("[LoginSecurity] " + translate(KICK_ALREADY_ONLINE));
                    return;
                }
            }
        }

        final String name = event.getName();
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isFilterSpecialChars() && !name.replaceAll("[^a-zA-Z0-9_]", "").equals(name)) {
            event.setLoginResult(Result.KICK_OTHER);
            event.setKickMessage("[LoginSecurity] " + translate(KICK_USERNAME_CHARS));
            return;
        }

        if(name.length() < config.getUsernameMinLength() || name.length() > config.getUsernameMaxLength()) {
            event.setLoginResult(Result.KICK_OTHER);
            event.setKickMessage("[LoginSecurity] " + translate(KICK_USERNAME_LENGTH)
                    .param("min", config.getUsernameMinLength()).param("max", config.getPasswordMaxLength()));
            return;
        }

        LoginSecurity.getSessionManager().preloadSession(event.getName(), event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Unload player
        LoginSecurity.getSessionManager().onPlayerLogout(event.getPlayer());
        MetaData.unset(event.getPlayer(), "ls_login_tries");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final PlayerProfile profile = session.getProfile();
        boolean saveAsync = false;
        if(profile.getLastName() == null || !player.getName().equals(profile.getLastName())) {
            profile.setLastName(player.getName());
            saveAsync = true;
        }

        // Admin update check
        if(session.isAuthorized() && player.hasPermission("ls.update")) {
            general.checkUpdates(player);
        }

        if(session.isAuthorized() || !session.isRegistered()) {
            if(session.isRegistered() && saveAsync) {
                session.saveProfileAsync();
            }
            return;
        }

        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        }

        // Clear inventory
        if(profile.getInventory() == null && config.isHideInventory()) {
            // Clear inventory
            final PlayerInventory inventory = player.getInventory();
            profile.setInventory(InventorySerializer.serializeInventory(inventory));
            inventory.clear();
            saveAsync = true;
        }

        // Reset location
        if(profile.getLoginLocation() == null) {
            final Location origin = player.getLocation().clone();
            switch(general.getLocationMode()) {
                case SPAWN:
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    profile.setLoginLocation(new PlayerLocation(origin));
                    saveAsync = true;
                    break;
                case RANDOM:
                    // TODO: Add random in.
                case DEFAULT:
                    break; // Do nothing (for now)
            }
        }

        if(saveAsync) {
            session.saveProfileAsync();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthChange(AuthModeChangedEvent event) {
        final PlayerSession session = event.getSession();
        final Player player = session.getPlayer();
        if(event.getCurrentMode() != AuthMode.AUTHENTICATED) {
            return;
        } if(!session.isLoggedIn() || !player.hasPermission("loginsecurity.update")) {
            return;
        }

        general.checkUpdates(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    /**
     * Player action filtering.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isUseCommandShortcut()) {
            if(event.getMessage().startsWith(config.getLoginCommandShortcut() + " ")) {
                event.setMessage("/login " + event.getMessage().substring(config.getLoginCommandShortcut().length()));
            } else if(event.getMessage().startsWith(config.getRegisterCommandShortcut() + " ")) {
                event.setMessage("/register " + event.getMessage().substring(config.getLoginCommandShortcut().length()));
            }
        }

        if(session.isAuthorized()) return;

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
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if(from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            event.setTo(event.getFrom());
        }
        // TODO: Set user to fly mode....
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        final Player player = (Player) event.getEntity();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return; // Not a player
        final Player player = (Player) event.getEntity();
        if(player.hasMetadata("NPC")) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTarget(EntityTargetEvent event) {
        if(!(event.getTarget() instanceof Player)) return; // Not a player
        final Player player = (Player) event.getTarget();
        if(!player.isOnline()) return; // Target logged out
        if(player.hasMetadata("NPC")) return; // Target is not human
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return; // Target is authenticated

        event.setCancelled(true);
    }
}
