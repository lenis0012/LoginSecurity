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

import com.lenis0012.bukkit.loginsecurity.database.LocationDao;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcLocationDao implements LocationDao {
    private final JdbcConnectionPool connectionPool;
    private final Logger logger;

    public JdbcLocationDao(JdbcConnectionPool connectionPool, Logger logger) {
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<PlayerLocation> findById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection =  connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * " +
                        "FROM ls_locations AS location " +
                        "WHERE location.id = ?;"
                );
                statement.setInt(1, id);
                return process(statement.executeQuery());
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to find profile by id in JDBC", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> insertLocation(PlayerLocation location) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteLocation(PlayerLocation location) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updateLocation(PlayerLocation location) {
        return null;
    }

    protected PlayerLocation process(ResultSet row) throws SQLException {
        if(row.getObject("location.id") == null) {
            // Does not have a location
            return null;
        }

        PlayerLocation location = new PlayerLocation();
        location.setId(row.getInt("location.id"));
        location.setWorld(row.getString("location.world"));
        location.setX(row.getDouble("location.x"));
        location.setY(row.getDouble("location.y"));
        location.setZ(row.getDouble("location.z"));
        location.setYaw(row.getInt("location.yaw"));
        location.setPitch(row.getInt("location.pitch"));
        return location;
    }
}
