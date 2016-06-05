package com.lenis0012.bukkit.loginsecurity.commands;

import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.modules.migration.AbstractMigration;
import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.pluginutils.modules.command.Command;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

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
            reply(false, "Unknown subcommand! Use /lac for help.");
            return;
        }

        SubCommand info = method.getAnnotation(SubCommand.class);
        if(getArgLength() < info.minArgs() + 1) {
            reply(false, "Not enough arguments! Use /lac for help.");
            return;
        }

        try {
            method.invoke(this);
        } catch(Exception e) {
            reply(false, "Error while executing command: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Error while executing command", e);
        }
    }

    @SubCommand(description = "Display command help")
    public void help() {
        reply("&3&lL&b&loginSecurity &3&lA&b&ldmin &3&lC&b&lommand:");
        for(Entry<String, Method> entry : methods.entrySet()) {
            String name = entry.getKey();
            SubCommand info = entry.getValue().getAnnotation(SubCommand.class);
            String usage = info.usage().isEmpty() ? "" : info.usage();
            reply("&b/" + name + usage + " &7- &f" + info.description());
        }
    }

    @SubCommand(description = "Import from another database", usage = "<source> [args]", minArgs = 1)
    public void dbimport() {
        MigrationModule module = plugin.getModule(MigrationModule.class);
        AbstractMigration migration = module.getMigration(getArg(1));
        if(migration == null) {
            reply(false, "Unknown database type, please check the wiki!");
            return;
        }

        String[] params = new String[getArgLength() - 2];
        for(int i = 0; i < params.length; i++) {
            params[i] = getArg(i + 2);
        }

        if(!migration.canExecute(params)) {
            reply(false, "Couldn't perform import, please check log!");
            return;
        }

        migration.execute(params);
    }
}
