package com.lenis0012.bukkit.ls.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;

public class ReflectionUtil {
	
	/*
	 * All credits of this method go to
	 * PlugMan, by ryanclancy000
	 */
	@SuppressWarnings("rawtypes")
	public static void unloadPlugin(String plugin) throws NoSuchFieldException, IllegalAccessException {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		SimplePluginManager spm = (SimplePluginManager)pm;
		SimpleCommandMap scm = null;
		List plugins = null;
		Map names = null;
		Map commands = null;
		Map listeners = null;
		boolean reloadListeners = true;
		
		if(spm != null) {
			Field pluginsField = spm.getClass().getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			plugins = (List)pluginsField.get(spm);
			
			Field lookupField = spm.getClass().getDeclaredField("lookupNames");
			lookupField.setAccessible(true);
			names = (Map)lookupField.get(spm);
			
			try {
				Field lField = spm.getClass().getDeclaredField("listeners");
				lField.setAccessible(true);
				listeners = (Map)lField.get(spm);
			} catch(Exception e) {
				reloadListeners = false;
			}
			
			Field cField = spm.getClass().getDeclaredField("commandMap");
			cField.setAccessible(true);
			scm = (SimpleCommandMap)cField.get(spm);
			
			Field cmdField = spm.getClass().getDeclaredField("knownCommands");
			cmdField.setAccessible(true);
			commands = (Map)cmdField.get(spm);
		}
		
		for(Plugin pl : pm.getPlugins()) {
			if(pl.getName().equalsIgnoreCase(plugin)) {
				pm.disablePlugin(pl);
				
				if(plugins != null && plugins.contains(pl)) {
					plugins.remove(pl);
				}
				
				if(names != null && names.containsKey(plugin)) {
					names.remove(plugin);
				}
				
				if(listeners != null && reloadListeners) {
					for (Object o : listeners.values()) {
						SortedSet set = (SortedSet)o;
						Iterator it = set.iterator();
						while(it.hasNext()) {
							RegisteredListener rl = (RegisteredListener)it.next();
							if(rl.getPlugin().getName().equalsIgnoreCase(plugin))
								it.remove();
						}
					}
				}
				
				if(scm != null) {
					Iterator it = commands.entrySet().iterator();
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						if(entry.getValue() instanceof PluginCommand) {
							PluginCommand c = (PluginCommand)entry.getValue();
							if(c.getPlugin().getName().equalsIgnoreCase(plugin)) {
								c.unregister(scm);
								it.remove();
							}
						}
					}
				}
			}
		}
	}
}
