package com.lenis0012.bukkit.loginsecurity.commands;

import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.modules.migration.AbstractMigration;
import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.bukkit.loginsecurity.session.AuthService;
import com.lenis0012.bukkit.loginsecurity.session.PlayerSession;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionCallback;
import com.lenis0012.bukkit.loginsecurity.session.action.ActionResponse;
import com.lenis0012.bukkit.loginsecurity.session.action.RemovePassAction;
import com.lenis0012.pluginutils.modules.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import static com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys.*;
import static com.lenis0012.bukkit.loginsecurity.LoginSecurity.translate;

public class CommandAdmin extends Command {
    private final Map<String, Method> methods = Maps.newLinkedHashMap(); // maintain order for help command
    private final LoginSecurity plugin;

    public CommandAdmin(LoginSecurity plugin) {
        this.plugin = plugin;
        setAllowConsole(true);
        setPermission("loginsecurity.admin");
        setUsage("/lac");
        for(Method method : getClass().getMethods()) {
            if(!method.isAnnotationPresent(SubCommand.class)) {
                continue;
            }
            methods.put(method.getName(), method);
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
            reply(false, translate(COMMAND_ERROR).param("error", e.getMessage()));
            plugin.getLogger().log(Level.SEVERE, "Error while executing command", e);
        }
    }

    @SubCommand(description = "lacHelp", minArgs = -1)
    public void help() {
        reply("&3&lL&b&loginSecurity &3&lA&b&ldmin &3&lC&b&lommand:");
        for(Entry<String, Method> entry : methods.entrySet()) {
            String name = entry.getKey();
            SubCommand info = entry.getValue().getAnnotation(SubCommand.class);
            String usage = info.usage().isEmpty() ? "" : translate(info.usage()).toString();
            reply("&b/" + name + usage + " &7- &f" + translate(info.description()));
        }
    }

    @SubCommand(description = "lacRmpass", usage = "lacRmpassArgs", minArgs = 1)
    public void rmpass() {
        String name = getArg(1);
        Player player = Bukkit.getPlayer(name);
        PlayerSession session = player != null ? LoginSecurity.getSessionManager().getPlayerSession(player) : LoginSecurity.getSessionManager().getOfflineSession(name);
        if(!session.isRegistered()) {
            reply(false, translate(LAC_NOT_REGISTERED));
        }

        final Player admin = player;
        session.performActionAsync(new RemovePassAction(AuthService.ADMIN, player), new ActionCallback() {
            @Override
            public void call(ActionResponse response) {
                reply(admin, true, translate(LAC_RESET_PLAYER));
            }
        });
    }

    @SubCommand(description = "lacImport", usage = "lacImportArgs", minArgs = 1)
    public void dbimport() {
        MigrationModule module = plugin.getModule(MigrationModule.class);
        AbstractMigration migration = module.getMigration(getArg(1));
        if(migration == null) {
            reply(false, translate(LAC_UNKNOWN_SOURCE));
            return;
        }

        String[] params = new String[getArgLength() - 2];
        for(int i = 0; i < params.length; i++) {
            params[i] = getArg(i + 2);
        }

        if(!migration.canExecute(params)) {
            reply(false, translate(LAC_IMPORT_FAILED));
            return;
        }

        migration.execute(params);
    }
}
