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

package com.lenis0012.bukkit.loginsecurity.database.jdbc;

import com.lenis0012.bukkit.loginsecurity.database.jdbc.platform.JdbcPlatform;

import javax.sql.ConnectionPoolDataSource;

import org.bukkit.configuration.ConfigurationSection;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

public class SQLiteTestPlatform extends JdbcPlatform {
    @Override
    public ConnectionPoolDataSource configure(ConfigurationSection configuration) {
        SQLiteConnectionPoolDataSource dataSource = new SQLiteConnectionPoolDataSource();
        dataSource.setDatabaseName(":memory:");
        return dataSource;
    }

    @Override
    public int getPingTimeout(ConfigurationSection configuration) {
        return 10;
    }

    @Override
    public int getMaximumPoolSize(ConfigurationSection configuration) {
        return 1;
    }
}
