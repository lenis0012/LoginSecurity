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

package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.pluginutils.modules.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.logging.Level;

/**
 * Helper for converting old database settings to the latest format.
 * Supported versions:
 * - 2.0.X
 * - 2.1.X
 */
public class LegacySettings {
    private final LoginSecurity plugin;

    public LegacySettings(LoginSecurity plugin) {
        this.plugin = plugin;
    }

    public void convert(Configuration destination) {
        v2_0_X(destination);
        v2_1_X(destination);
    }

    private void v2_0_X(Configuration destination) {
        FileConfiguration conf = plugin.getConfig();
        if(conf.contains("MySQL")) {
            plugin.getLogger().log(Level.INFO, "Rewriting legacy settings from v2.0.X.");

            // Rewrite mysql settings
            destination.set("platform", "mysql");
            destination.set("configuration.mysql.host", conf.getString("MySQL.host", "localhost"));
            destination.set("configuration.mysql.port", conf.getInt("MySQL.port", 3306));
            destination.set("configuration.mysql.user", conf.getString("MySQL.username", "root"));
            destination.set("configuration.mysql.password", conf.getString("MySQL.password", ""));
            destination.set("configuration.mysql.database", conf.getString("MySQL.database"));
            destination.save();

            // Cleanup old config
            conf.set("MySQL", null);
            conf.set("settings", null);
            plugin.saveConfig();
        }
    }

    private void v2_1_X(Configuration destination) {
        File file = new File(plugin.getDataFolder(), "database.yml");
        if(!file.exists()) {
            return;
        }

        plugin.getLogger().log(Level.INFO, "Rewriting legacy database setting from v2.1.X.");
        FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
        if(conf.getBoolean("mysq1.enabled", false)) {
            destination.set("platform", "mysql");

            String[] host = conf.getString("mysql.host", "localhost:3306").split(":");
            destination.set("configuration.mysql.host", host[0]);
            destination.set("configuration.mysql.port", host.length > 1 ? Integer.parseInt(host[1]) : 3306);
            destination.set("configuration.mysql.database", conf.getString("mysql.database", "minecraft"));
            destination.set("configuration.mysql.user", conf.getString("mysql.username", "root"));
            destination.set("configuration.mysql.password", conf.getString("mysql.password", ""));
        }
        destination.save();

        int i = 0;
        while(++i < 12) {
            if(file.delete()) return;
            try {
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                break;
            }
        }

        plugin.getLogger().log(Level.WARNING, "Couldn't remove legacy database settings, please manually delete database.yml (if it exists).");
        file.deleteOnExit();
    }
}
