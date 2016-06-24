package com.lenis0012.bukkit.loginsecurity.modules.migration;

import com.google.common.collect.Maps;
import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.pluginutils.Module;

import java.util.Map;

public class MigrationModule extends Module<LoginSecurity> {
    private final Map<String, AbstractMigration> migrations = Maps.newConcurrentMap();

    public MigrationModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // Create all migrations
        final AbstractMigration[] values = new AbstractMigration[] {
                new LegacyMigration(),
                new AuthmeMigration(),
                new xAuthMigration()
        };

        // List all migrations
        for(AbstractMigration migration : values) {
            migrations.put(migration.getName().toLowerCase(), migration);
        }

        // Execute migrations if needed
        for(AbstractMigration migration : values) {
            if(migration.executeAutomatically()) {
                migration.execute(new String[0]);
            }
        }
    }

    @Override
    public void disable() {
    }

    public AbstractMigration getMigration(String name) {
        return migrations.get(name.toLowerCase());
    }
}
