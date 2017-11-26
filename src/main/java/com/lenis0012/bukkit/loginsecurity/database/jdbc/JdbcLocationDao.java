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
import com.lenis0012.bukkit.loginsecurity.storage.AbstractEntity;
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
    public PlayerLocation findById(int id) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * " +
                    "FROM ls_locations " +
                    "WHERE id = ?;"
            );
            statement.setInt(1, id);
            return process(statement.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to find profile by id in JDBC", e);
        }
        return null;
    }

    @Override
    public int insertLocation(PlayerLocation location) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_locations (world, x, y, z, yaw, pitch) " +
                    "VALUES (?,?,?,?,?,?);",
                    new String[] { "id" }
            );

            statement.setString(1, location.getWorld());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setInt(5, location.getYaw());
            statement.setInt(6, location.getPitch());

            ResultSet keys = statement.getGeneratedKeys();
            if(!keys.next()) {
                throw new RuntimeException("No keys were returned after insert");
            }

            return keys.getInt("id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    @Override
    public boolean deleteLocation(PlayerLocation location) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM ls_locations WHERE id = ?;");
            statement.setInt(1, location.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    @Override
    public boolean updateLocation(PlayerLocation location) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE ls_locations " +
                    "SET world=?,x=?,y=?,z=?,yaw=?,pitch=? " +
                    "WHERE id = ?;"
            );
            statement.setString(1, location.getWorld());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setInt(5, location.getYaw());
            statement.setInt(6, location.getPitch());
            statement.setInt(7, location.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    PlayerLocation process(ResultSet row) throws SQLException {
        if(row.getObject("location_id") == null) {
            // Does not have a location
            return null;
        }

        PlayerLocation location = new PlayerLocation();
        location.setId(row.getInt("location_id"));
        location.setWorld(row.getString("world"));
        location.setX(row.getDouble("x"));
        location.setY(row.getDouble("y"));
        location.setZ(row.getDouble("z"));
        location.setYaw(row.getInt("yaw"));
        location.setPitch(row.getInt("pitch"));

        location.setState(AbstractEntity.State.UNCHANGED);
        return location;
    }
}
