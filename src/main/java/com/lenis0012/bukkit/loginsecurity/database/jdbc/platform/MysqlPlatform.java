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

package com.lenis0012.bukkit.loginsecurity.database.jdbc.platform;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.bukkit.configuration.ConfigurationSection;

import javax.sql.ConnectionPoolDataSource;

public class MysqlPlatform implements JdbcPlatform {

    @Override
    public ConnectionPoolDataSource configure(ConfigurationSection configuration) {
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(configuration.getString("host", "localhost"));
        dataSource.setPort(configuration.getInt("port", 3306));
        dataSource.setDatabaseName(configuration.getString("database", "minecraft"));
        dataSource.setUser(configuration.getString("user", "root"));
        dataSource.setPassword(configuration.getString("password", ""));

        // Optimizations (mainly caching)
        dataSource.setCachePreparedStatements(true);
        dataSource.setUseServerPrepStmts(true);
        dataSource.setUseLocalSessionState(true);
        dataSource.setUseLocalTransactionState(true);
        dataSource.setRewriteBatchedStatements(true);
        dataSource.setCacheResultSetMetadata(true);
        dataSource.setCacheServerConfiguration(true);
        dataSource.setElideSetAutoCommits(true);
        dataSource.setMaintainTimeStats(false);

        return dataSource;
    }

    @Override
    public int getPingTimeout(ConfigurationSection configuration) {
        return configuration.getInt("ping-timeout", 10);
    }
}
