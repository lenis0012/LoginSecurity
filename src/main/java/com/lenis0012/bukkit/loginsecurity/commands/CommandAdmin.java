package com.lenis0012.bukkit.loginsecurity.commands;

import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.modules.general.GeneralModule;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageImport;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ChangePassAction;
import com.lenis0012.bukkit.loginsecurity.session.action.RemovePassAction;
import com.lenis0012.pluginutils.modules.command.Command;
import com.lenis0012.updater.api.Updater;
import com.lenis0012.updater.api.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;
import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;

public class CommandAdmin extends Command {
    private final Map<String, Method> methods = Maps.newLinkedHashMap(); // maintain order for help command
    private final LoginSecurity plugin;

    public CommandAdmin(LoginSecurity plugin) {
        this.plugin = plugin;
        setAllowConsole(true);
        setPermission("loginsecurity.admin");
        setUsage("/lac");
        for(Method method : getClass().getMethods()) {
            SubCommand subCommand = method.getAnnotation(SubCommand.class);
            if(subCommand == null) {
                continue;
            }

            methods.put(subCommand.name().isEmpty() ? method.getName() : subCommand.name(), method);
        }
    }

    @Override
    public void execute() {
        String subCommand = getArgLength() > 0 ? getArg(0) : "help";
        Method method = methods.get(subCommand.toLowerCase());
        if(method == null) {
            reply(false, translate(COMMAND_UNKNOWN).param("cmd", "/lac"));
            return;
        }

        SubCommand info = method.getAnnotation(SubCommand.class);
        if(getArgLength() < info.minArgs() + 1) {
            reply(false, translate(COMMAND_NOT_ENOUGH_ARGS).param("cmd", "/lac"));
            return;
        }

        try {
            method.invoke(this);
        } catch(Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error while executing command", e);
            reply(false, translate(COMMAND_ERROR)
                    .param("error", e.getMessage() != null ? e.getMessage() : ""));
        }
    }

    @SubCommand(description = "lacHelp", minArgs = -1)
    public void help() {
        reply("&3&lL&b&loginSecurity &3&lA&b&ldmin &3&lC&b&lommand:");
        for(Entry<String, Method> entry : methods.entrySet()) {
            String name = entry.getKey();
            SubCommand info = entry.getValue().getAnnotation(SubCommand.class);
            String usage = info.usage().isEmpty() ? "" : " " +
                    (info.usage().startsWith("NoTrans:") ?
                            info.usage().substring("NoTrans:".length()) :
                            translate(info.usage()).toString());
            String desc = info.description().startsWith("NoTrans:") ?
                    info.description().substring("NoTrans:".length()) :
                    translate(info.description()).toString();
            reply("&b/lac " + name + usage + " &7- &f" + desc);
        }
    }

    @SubCommand(description = "lacReload")
    public void reload() {
        LoginSecurity.getConfiguration().reload();
        reply(true, translate(LAC_RELOAD_SUCCESS));
    }

    @SubCommand(description = "lacRmpass", usage = "lacRmpassArgs", minArgs = 1)
    public void rmpass() {
        String name = getArg(1);
        Player target = Bukkit.getPlayer(name);
        PlayerSession session = target != null ? LoginSecurity.getSessionManager().getPlayerSession(target) : LoginSecurity.getSessionManager().getOfflineSession(name);
        if(!session.isRegistered()) {
            reply(false, translate(LAC_NOT_REGISTERED));
            return;
        }

        final CommandSender admin = sender;
        session.performActionAsync(
                new RemovePassAction(AuthService.ADMIN, admin),
                response -> reply(admin, true, translate(LAC_RESET_PLAYER))
        );
    }

    @SubCommand(description = "lacChangepass", usage = "lacChangepassArgs", minArgs = 2)
    public void changepass() {
        String name = getArg(1);
        Player target = Bukkit.getPlayer(name);
        PlayerSession session = target != null ? LoginSecurity.getSessionManager().getPlayerSession(target) : LoginSecurity.getSessionManager().getOfflineSession(name);
        if(!session.isRegistered()) {
            reply(false, translate(LAC_NOT_REGISTERED));
            return;
        }

        final CommandSender admin = sender;
        session.performActionAsync(
                new ChangePassAction(AuthService.ADMIN, admin, getArg(2)),
                response -> reply(admin, true, translate(LAC_CHANGED_PASSWORD))
        );
    }

    @SubCommand(name = "import", description = "NoTrans:Import profiles into LoginSecurity", usage = "NoTrans:loginsecurity", minArgs = 1)
    public void importFrom() {
        String source = getArg(1);
        StorageImport storageImport = StorageImport.fromSourceName(source, sender);
        if(storageImport == null) {
            reply(false, "Unknown import source: " + source);
            return;
        }

        reply(true, "Importing profiles from " + source);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, storageImport);
    }

    @SubCommand(description = "NoTrans:Download update from bukkit/spigot")
    public void update() {
        final Updater updater = plugin.getModule(GeneralModule.class).getUpdater();
        final Version version = updater.getNewVersion();
        if(version == null) {
            reply(false, "Updater is not enabled!");
            return;
        }

        if(!updater.hasUpdate()) {
            reply(false, "No updated available!");
            return;
        }

        reply(true, "Downloading " + version.getName() + "...");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String message = updater.downloadVersion();
            final String response = message == null ? "&aUpdate successful, will be active on reboot." : "&c&lError: &c" + message;
            Bukkit.getScheduler().runTask(plugin, () -> {
                reply(response);

                ItemStack changelog = updater.getChangelog();
                if(changelog == null) {
                    reply("&cChangelog isn't available for this version.");
                    return;
                }

                ItemStack inHand = player.getItemInHand();
                player.setItemInHand(changelog);
                if(inHand != null) {
                    player.getInventory().addItem(inHand);
                }

                reply("&llenis> &bCheck my changelog out! (I put it in your hand)");
                player.updateInventory();
            });
        });
    }
}
