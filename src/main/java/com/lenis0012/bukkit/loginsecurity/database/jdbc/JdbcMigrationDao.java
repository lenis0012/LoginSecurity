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

import com.lenis0012.bukkit.loginsecurity.storage.Migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JdbcMigrationDao {
    private final JdbcConnectionPool connectionPool;
    private final Logger logger;

    public JdbcMigrationDao(JdbcConnectionPool connectionPool, Logger logger) {
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

    public List<Migration> findAll() {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * " +
                    "FROM ls_upgrades;"
            );

            List<Migration> list = new ArrayList<>();
            ResultSet rows = statement.executeQuery();
            while(rows.next()) {
                list.add(process(rows));
            }
            return list;
        } catch(SQLException e) {
            return new ArrayList<>();
        }
    }

    public int insertMigration(Migration migration) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_upgrades (version, description, applied_at) " +
                    "VALUES (?,?,?);",
                    new String[] { "id" }
            );
            statement.setString(1, migration.getVersion());
            statement.setString(2, migration.getName());
            statement.setTimestamp(3, migration.getAppliedAt());

            ResultSet keys = statement.getGeneratedKeys();
            if(!keys.next()) {
                throw new RuntimeException("Migration insert didn't return any keys");
            }

            return keys.getInt("id");
        } catch(SQLException e) {
            throw new RuntimeException("Error occurred while finding migration list", e);
        }
    }

    private Migration process(ResultSet row) throws SQLException {
        Migration migration = new Migration();
        migration.setId(row.getInt("id"));
        migration.setName(row.getString("description"));
        migration.setVersion(row.getString("version"));
        migration.setAppliedAt(row.getTimestamp("applied_at"));
        return migration;
    }
}
