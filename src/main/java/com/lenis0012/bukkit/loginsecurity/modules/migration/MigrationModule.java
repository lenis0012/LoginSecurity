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
//                new LegacyMigration(),
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
