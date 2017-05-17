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
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcProfileDao implements ProfileDao {
    private final JdbcConnectionPool connectionPool;
    private final Logger logger;

    JdbcProfileDao(JdbcConnectionPool connectionPool, Logger logger) {
        this.connectionPool = connectionPool;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<PlayerProfile> findById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection =  connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM ls_players WHERE id=?;");
                statement.setInt(1, id);
                return process(statement.executeQuery());
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to find profile by id in JDBC", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<PlayerProfile> findByUniqueUserId(UUID uniqueUserId) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM ls_players WHERE unique_user_id=?;");
                statement.setString(1, uniqueUserId.toString());
                ResultSet result = statement.executeQuery();
                return result.next() ? process(result) : null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
            }
        });
    }

    @Override
    public CompletableFuture<PlayerProfile> findByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM ls_players WHERE last_name=?;");
                statement.setString(1, username);
                ResultSet result = statement.executeQuery();
                return result.next() ? process(result) : null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<PlayerProfile>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM ls_players;");
                List<PlayerProfile> profiles = new ArrayList<>();

                ResultSet rows = statement.executeQuery();
                while(rows.next()) {
                    profiles.add(process(rows));
                }
                return profiles;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
            }
        });
    }

    @Override
    public CompletableFuture<Iterator<PlayerProfile>> iterateAll() {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM ls_players;");

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
        });
    }

    @Override
    public CompletableFuture<Integer> insertProfile(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO ls_players(uuid_mode,unique_user_id,last_name,ip_address,password,hashing_algorithm,last_login,registration_date,optlock) VALUES(?,?,?,?,?,?,?,?,?);",
                        new String[] { "id" }
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
                ResultSet keys = statement.getGeneratedKeys();
                if(!keys.next()) {
                    throw new RuntimeException("No keys were returned after insert");
                }

                return keys.getInt("id");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteProfile(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM ls_players WHERE id=?;");
                statement.setInt(1, profile.getId());
                return statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find profile by uuid in JDBC", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> updateProfile(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection connection = connectionPool.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE SET last_name=?,ip_address=?,password=?,hashing_algorithm=?,last_login=?,optlock=? WHERE id=?;");
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
        });
    }

    private PlayerProfile process(ResultSet row) throws SQLException {
        PlayerProfile profile = new PlayerProfile();
        profile.setId(row.getInt("id"));
        profile.setUniqueIdMode(UserIdMode.fromId(row.getString("uuid_mode")));
        profile.setUniqueUserId(row.getString("unique_user_id"));
        profile.setLastName(row.getString("last_name"));
        profile.setIpAddress(row.getString("ip_address"));
        profile.setPassword(row.getString("password"));
        profile.setHashingAlgorithm(row.getInt("hashing_algorithm"));
        profile.setLastLogin(row.getTimestamp("last_login"));
        profile.setRegistrationDate(row.getDate("registration_date"));
        profile.setVersion(row.getLong("optlock"));

        // TODO: Set relations

        return profile;
    }
}
