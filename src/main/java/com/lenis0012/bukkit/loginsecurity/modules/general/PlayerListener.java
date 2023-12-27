package com.lenis0012.bukkit.loginsecurity.modules.general;

import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import com.lenis0012.bukkit.loginsecurity.events.AuthModeChangedEvent;
import com.lenis0012.bukkit.loginsecurity.session.AuthMode;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.BypassAction;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.List;
import java.util.logging.Level;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // Check if player already online
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

        // Verify name
        final String name = event.getName();
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isFilterSpecialChars() && !name.replaceAll("[^a-zA-Z0-9_]", "").equals(name)) {
            event.setLoginResult(Result.KICK_OTHER);
            event.setKickMessage("[LoginSecurity] " + translate(KICK_USERNAME_CHARS));
            return;
        }

        // Verify name length
        if(name.length() < config.getUsernameMinLength() || name.length() > config.getUsernameMaxLength()) {
            event.setLoginResult(Result.KICK_OTHER);
            event.setKickMessage("[LoginSecurity] " + translate(KICK_USERNAME_LENGTH)
                    .param("min", config.getUsernameMinLength()).param("max", config.getPasswordMaxLength()));
            return;
        }

        // Pre-load player to improve performance...
        final PlayerSession session = LoginSecurity.getSessionManager().preloadSession(event.getName(), event.getUniqueId());

        // Dis-allow joining if a differently-cased version of the same name is used.
        if(LoginSecurity.getConfiguration().isMatchUsernameExact() &&
                session.getProfile().getUniqueIdMode() == UserIdMode.OFFLINE &&
                session.getProfile().getLastName() != null &&
                !event.getName().equals(session.getProfile().getLastName())) {
            event.setLoginResult(Result.KICK_OTHER);
            event.setKickMessage("[LoginSecurity] " + translate(KICK_USERNAME_REGISTERED)
                    .param("username", session.getProfile().getLastName()));
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);

        if(!session.isRegistered() && player.isPermissionSet("ls.bypass") && player.hasPermission("ls.bypass")) {
            session.performAction(new BypassAction(AuthService.PLAYER, player));
        }
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
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final PlayerProfile profile = session.getProfile();
        if(profile.getLastName() == null || !player.getName().equals(profile.getLastName())) {
            profile.setLastName(player.getName());
            if(session.isRegistered()) {
                session.saveProfileAsync();
            }
        }

        // Admin update check
        if(session.isAuthorized() && player.hasPermission("ls.update")) {
            general.checkUpdates(player);
        }

        if(session.isAuthorized() || !session.isRegistered()) {
            return;
        }

        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isBlindness()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
        }
    }

    @EventHandler
    public void maskPlayerLocation(PlayerSpawnLocationEvent event) {
        final Player player = event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(general.getLocationMode() != LocationMode.SPAWN) {
            return;
        }
        if(!session.isRegistered()) {
            return; // Can't store location for non-registered players. and it shouldn't happen anyway
        }
        // Don't update location if already done in previous login
        if(session.getProfile().getLoginLocationId() != null) {
            return;
        }

        PlayerLocation rememberedLocation = new PlayerLocation(event.getSpawnLocation());
        event.setSpawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
        LoginSecurity.getDatastore().getLocationRepository().insertLoginLocation(session.getProfile(), rememberedLocation, result -> {
            if(!result.isSuccess()) {
                LoginSecurity.getInstance().getLogger().log(Level.SEVERE, "Failed to save player location", result.getError());
                player.teleport(rememberedLocation.asLocation());
            } else if(session.isAuthorized() && player.isOnline()) {
                LoginSecurity.getInstance().getLogger().log(Level.WARNING, "Player was logged in prematurely while still saving location");
                PaperLib.teleportAsync(player, rememberedLocation.asLocation());
                session.getProfile().setLoginLocationId(null);
                session.saveProfileAsync();
                LoginSecurity.getDatastore().getLocationRepository().delete(rememberedLocation);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAuthChange(AuthModeChangedEvent event) {
        final PlayerSession session = event.getSession();
        if(event.getCurrentMode() != AuthMode.AUTHENTICATED) {
            return;
        }

        final Player player = session.getPlayer();
        if(player == null || !session.isLoggedIn() || !player.hasPermission("ls.update")) {
            return;
        }

        general.checkUpdates(player);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(isInvalidPlayer(event.getPlayer())) return;
        final Player player = (Player) event.getPlayer();
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        // Prevent moving
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        defaultEventAction(event);
    }

    /**
     * Player action filtering.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        final LoginSecurityConfig config = LoginSecurity.getConfiguration();
        if(config.isUseCommandShortcut()) {
            if(event.getMessage().toLowerCase().startsWith(config.getLoginCommandShortcut() + " ")) {
                event.setMessage("/login " + event.getMessage().substring(config.getLoginCommandShortcut().length() + 1));
            } else if(event.getMessage().toLowerCase().startsWith(config.getRegisterCommandShortcut() + " ")) {
                event.setMessage("/register " + event.getMessage().substring(config.getRegisterCommandShortcut().length()  + 1));
            } else if(event.getMessage().equalsIgnoreCase(config.getLoginCommandShortcut())) {
                event.setMessage("/login");
            } else if(event.getMessage().equalsIgnoreCase(config.getRegisterCommandShortcut())) {
                event.setMessage("/register");
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
        defaultEventAction(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if(isInvalidPlayer(player)) return;
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
        defaultEventAction(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        defaultEventAction(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        final Player player = (Player) event.getEntity();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER) return; // Not a player
        final Player player = (Player) event.getEntity();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTarget(EntityTargetEvent event) {
        if(!(event.getTarget() instanceof Player)) return; // Not a player
        final Player player = (Player) event.getTarget();
        if(!player.isOnline()) return; // Target logged out
        if(isInvalidPlayer(player)) return; // Target is not human
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return; // Target is authenticated

        event.setCancelled(true);
    }

    private void defaultEventAction(PlayerEvent event) {
        if(!(event instanceof Cancellable)) {
            throw new IllegalArgumentException("Event cannot be cancelled!");
        }
        final Player player = event.getPlayer();
        if(isInvalidPlayer(player)) return;
        final PlayerSession session = LoginSecurity.getSessionManager().getPlayerSession(player);
        if(session.isAuthorized()) return;

        ((Cancellable) event).setCancelled(true);
    }

    private boolean isInvalidPlayer(HumanEntity human) {
        if(!(human instanceof Player)) return true;
        final Player player = (Player) human;
        return player.hasMetadata("NPC") || !player.isOnline();
    }
}
