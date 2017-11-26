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

import com.lenis0012.bukkit.loginsecurity.database.ProfileDao;
import com.lenis0012.bukkit.loginsecurity.storage.AbstractEntity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
//import com.mysql.jdbc.ResultSetMetaData;
//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcProfileDao implements ProfileDao {
    private final JdbcDaoFactory daoFactory;
    private final JdbcConnectionPool connectionPool;
    private final Logger logger;

    JdbcProfileDao(JdbcDaoFactory daoFactory, JdbcConnectionPool connectionPool, Logger logger) {
        this.daoFactory = daoFactory;
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

//    public static void main(String[] args) throws Exception {
//        MysqlDataSource dataSource = new MysqlDataSource();
////        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/minecraft");
//        dataSource.setServerName("127.0.0.1");
//        dataSource.setDatabaseName("minecraft");
//        dataSource.setUser("root");
//
//        try(Connection connection = dataSource.getConnection()) {
//            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ls_players AS player LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id WHERE player.id = 1;");
//            ResultSet result = ps.executeQuery();
//            System.out.println(result.next());
//            System.out.println(result.getString("inventory.id"));
//
//            com.mysql.jdbc.ResultSetMetaData metaData = (ResultSetMetaData) result.getMetaData();
//            for(int col = 1; col <= metaData.getColumnCount(); col++) {
//                System.out.println(metaData.getColumnLabel(col) + "=" + result.getObject(col));
//            }
//        }
//    }

    @Override
    public PlayerProfile findById(int id) {
        try(Connection connection =  connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "player.id AS player_id, player.unique_user_id, player.uuid_mode, player.last_name, player.ip_address, player.password, player.hashing_algorithm, player.last_login, player.registration_date, player.optlock," +
                    "location.id AS location_id, location.world, location.x, location.y, location.z, location.yaw, location.pitch," +
                    "inventory.id AS inventory_id, inventory.helmet, inventory.chestplate, inventory.leggings, inventory.boots, inventory.off_hand, inventory.contents " +
                    "FROM ls_players AS player " +
                    "LEFT JOIN ls_locations AS location ON player.location_id = location.id " +
                    "LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id " +
                    "WHERE player.id = ?;"
            );
            statement.setInt(1, id);
            return process(statement.executeQuery());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to find profile by id in JDBC", e);
        }
        return null;
    }

    @Override
    public PlayerProfile findByUniqueUserId(UUID uniqueUserId) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "player.id AS player_id, player.unique_user_id, player.uuid_mode, player.last_name, player.ip_address, player.password, player.hashing_algorithm, player.last_login, player.registration_date, player.optlock," +
                    "location.id AS location_id, location.world, location.x, location.y, location.z, location.yaw, location.pitch," +
                    "inventory.id AS inventory_id, inventory.helmet, inventory.chestplate, inventory.leggings, inventory.boots, inventory.off_hand, inventory.contents " +
                    "FROM ls_players AS player " +
                    "LEFT JOIN ls_locations AS location ON player.location_id = location.id " +
                    "LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id " +
                    "WHERE player.unique_user_id=?;"
            );
            statement.setString(1, uniqueUserId.toString());
            ResultSet result = statement.executeQuery();
            return result.next() ? process(result) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public PlayerProfile findByUsername(String username) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "player.id AS player_id, player.unique_user_id, player.uuid_mode, player.last_name, player.ip_address, player.password, player.hashing_algorithm, player.last_login, player.registration_date, player.optlock," +
                    "location.id AS location_id, location.world, location.x, location.y, location.z, location.yaw, location.pitch," +
                    "inventory.id AS inventory_id, inventory.helmet, inventory.chestplate, inventory.leggings, inventory.boots, inventory.off_hand, inventory.contents " +
                    "FROM ls_players AS player " +
                    "LEFT JOIN ls_locations AS location ON player.location_id = location.id " +
                    "LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id " +
                    "WHERE player.last_name=?;"
            );
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            return result.next() ? process(result) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public List<PlayerProfile> findAll() {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " +
                    "player.id AS player_id, player.unique_user_id, player.uuid_mode, player.last_name, player.ip_address, player.password, player.hashing_algorithm, player.last_login, player.registration_date, player.optlock," +
                    "location.id AS location_id, location.world, location.x, location.y, location.z, location.yaw, location.pitch," +
                    "inventory.id AS inventory_id, inventory.helmet, inventory.chestplate, inventory.leggings, inventory.boots, inventory.off_hand, inventory.contents " +
                    "FROM ls_players AS player " +
                    "LEFT JOIN ls_locations AS location ON player.location_id = location.id " +
                    "LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id;"
            );
            List<PlayerProfile> profiles = new ArrayList<>();

            ResultSet rows = statement.executeQuery();
            while(rows.next()) {
                profiles.add(process(rows));
            }
            return profiles;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public Iterator<PlayerProfile> iterateAll() {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT *  " +
                    "FROM ls_players AS player " +
                    "LEFT JOIN ls_locations AS location ON player.location_id = location.id " +
                    "LEFT JOIN ls_inventories AS inventory ON player.inventory_id = inventory.id;"
            );

            ResultSet rows = statement.executeQuery();
            return new Iterator<PlayerProfile>() {
                @Override
                public boolean hasNext() {
                    try {
                        return rows.next();
                    } catch (SQLException e) {
                        throw new RuntimeException("Error occurred in sql iterator", e);
                    }
                }

                @Override
                public PlayerProfile next() {
                    try {
                        return process(rows);
                    } catch (SQLException e) {
                        throw new RuntimeException("Error occurred in sql iterator", e);
                    }
                }
            };
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public int insertProfile(PlayerProfile profile) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ls_players(uuid_mode,unique_user_id,last_name,ip_address,password,hashing_algorithm,last_login,registration_date,optlock) VALUES(?,?,?,?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, profile.getUniqueIdMode().getId());
            statement.setString(2, profile.getUniqueUserId());
            statement.setString(3, profile.getLastName());
            statement.setString(4, profile.getIpAddress());
            statement.setString(5, profile.getPassword());
            statement.setInt(6, profile.getHashingAlgorithm());
            statement.setTimestamp(7, Timestamp.from(Instant.now()));
            statement.setDate(8, new Date(System.currentTimeMillis()));
            statement.setLong(9, 1);
            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if(!keys.next()) {
                throw new RuntimeException("No keys were returned after insert");
            }

            int id = keys.getInt(1);
            profile.setId(id);
            return id;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public boolean deleteProfile(PlayerProfile profile) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM ls_players WHERE id=?;");
            statement.setInt(1, profile.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    @Override
    public boolean updateProfile(PlayerProfile profile) {
        try(Connection connection = connectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE ls_players SET last_name=?,ip_address=?,password=?,hashing_algorithm=?,last_login=?,optlock=? WHERE id=?;");
            statement.setString(1, profile.getLastName());
            statement.setString(2, profile.getIpAddress());
            statement.setString(3, profile.getPassword());
            statement.setInt(4, profile.getHashingAlgorithm());
            statement.setTimestamp(5, Timestamp.from(Instant.now()));
            statement.setLong(6, profile.getVersion() + 1);
            statement.setInt(7, profile.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
        }
    }

    private PlayerProfile process(ResultSet row) throws SQLException {
        PlayerProfile profile = new PlayerProfile();

        // Read object
        profile.setId(row.getInt("player_id"));
        profile.setUniqueIdMode(UserIdMode.fromId(row.getString("uuid_mode")));
        profile.setUniqueUserId(row.getString("unique_user_id"));
        profile.setLastName(row.getString("last_name"));
        profile.setIpAddress(row.getString("ip_address"));
        profile.setPassword(row.getString("password"));
        profile.setHashingAlgorithm(row.getInt("hashing_algorithm"));
        profile.setLastLogin(row.getTimestamp("last_login"));
        profile.setRegistrationDate(row.getDate("registration_date"));
        profile.setVersion(row.getLong("optlock"));

        // Read embedded objects
        profile.setLoginLocation(daoFactory.locationDao.process(row));
        profile.setInventory(daoFactory.inventoryDao.process(row));

        profile.setState(AbstractEntity.State.UNCHANGED);
        return profile;
    }
}
