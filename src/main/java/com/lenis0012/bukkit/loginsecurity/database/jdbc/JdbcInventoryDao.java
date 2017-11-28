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

import com.lenis0012.bukkit.loginsecurity.database.InventoryDao;
import com.lenis0012.bukkit.loginsecurity.storage.AbstractEntity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerInventory;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcInventoryDao implements InventoryDao {
    private final JdbcConnectionPool connectionPool;
    private final Logger logger;

    public JdbcInventoryDao(JdbcConnectionPool connectionPool, Logger logger) {
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

    @Override
    public PlayerInventory findById(int id) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * " +
                    "FROM ls_inventories " +
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
    public int insertInventory(PlayerInventory inventory) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_inventories (helmet, chestplate, leggings, boots, off_hand, contents) " +
                    "VALUES (?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, inventory.getHelmet());
            statement.setString(2, inventory.getChestplate());
            statement.setString(3, inventory.getLeggings());
            statement.setString(4, inventory.getBoots());
            statement.setString(5, inventory.getOffHand());
            statement.setString(6, inventory.getContents());
            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if(!keys.next()) {
                throw new RuntimeException("No keys were returned after insert");
            }

            int id = keys.getInt(1);
            inventory.setId(id);
            return id;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    @Override
    public boolean deleteInventory(PlayerInventory inventory) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM ls_inventories WHERE id = ?;");
            statement.setInt(1, inventory.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    @Override
    public boolean updateInventory(PlayerInventory inventory) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE ls_inventories " +
                    "SET helmet=?,chestplate=?,leggings=?,boots=?,off_hand=?,contents=? " +
                    "WHERE id = ?;"
            );
            statement.setString(1, inventory.getHelmet());
            statement.setString(2, inventory.getChestplate());
            statement.setString(3, inventory.getLeggings());
            statement.setString(4, inventory.getBoots());
            statement.setString(5, inventory.getOffHand());
            statement.setString(6, inventory.getContents());
            statement.setInt(7, inventory.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by id in JDBC", e);
        }
    }

    PlayerInventory process(ResultSet row) throws SQLException {
        if(row.getObject("inventory_id") == null) {
            // Inventory doesn't exist
            return null;
        }

        PlayerInventory inventory = new PlayerInventory();
        inventory.setId(row.getInt("inventory_id"));
        inventory.setHelmet(row.getString("helmet"));
        inventory.setChestplate(row.getString("chestplate"));
        inventory.setLeggings(row.getString("leggings"));
        inventory.setBoots(row.getString("boots"));
        inventory.setOffHand(row.getString("off_hand"));
        inventory.setContents(row.getString("contents"));

        inventory.setState(AbstractEntity.State.UNCHANGED);
        return inventory;
    }
}
