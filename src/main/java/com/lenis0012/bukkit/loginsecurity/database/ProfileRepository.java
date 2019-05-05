package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.function.Consumer;

public class ProfileRepository {
    private final LoginSecurity loginSecurity;
    private final DataSource dataSource;

    public ProfileRepository(LoginSecurity loginSecurity, DataSource dataSource) {
        this.loginSecurity = loginSecurity;
        this.dataSource = dataSource;
    }

    public void insert(PlayerProfile profile, Consumer<AsyncResult<PlayerProfile>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                insertBlocking(profile);
                resolveResult(callback, profile);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void insertBlocking(PlayerProfile profile) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ls_players(uuid_mode,unique_user_id,last_name,ip_address,password,hashing_algorithm,location_id,inventory_id,last_login,registration_date,optlock) VALUES(?,?,?,?,?,?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, profile.getUniqueIdMode().getId());
                statement.setString(2, profile.getUniqueUserId());
                statement.setString(3, profile.getLastName());
                statement.setString(4, profile.getIpAddress());
                statement.setString(5, profile.getPassword());
                statement.setInt(6, profile.getHashingAlgorithm());
                statement.setObject(7, profile.getLoginLocation() != null ? profile.getLoginLocation().getId() : null, Types.INTEGER);
                statement.setObject(8, profile.getInventory() != null ? profile.getInventory().getId() : null, Types.INTEGER);
                statement.setTimestamp(9, Timestamp.from(Instant.now()));
                statement.setDate(10, new Date(System.currentTimeMillis()));
                statement.setLong(11, 1);
                statement.executeUpdate();

                try(ResultSet keys = statement.getGeneratedKeys()) {
                    if(!keys.next()) {
                        throw new RuntimeException("No keys were returned after insert");
                    }
                    profile.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(PlayerProfile profile, Consumer<AsyncResult<PlayerProfile>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                updateBlocking(profile);
                resolveResult(callback, profile);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void updateBlocking(PlayerProfile profile) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE ls_players SET last_name=?,ip_address=?,password=?,hashing_algorithm=?,location_id=?,inventory_id=?,last_login=?,optlock=? WHERE id=?;")) {

                statement.setString(1, profile.getLastName());
                statement.setString(2, profile.getIpAddress());
                statement.setString(3, profile.getPassword());
                statement.setInt(4, profile.getHashingAlgorithm());
                statement.setObject(5, profile.getLoginLocation() != null ? profile.getLoginLocation().getId() : null, Types.INTEGER);
                statement.setObject(6, profile.getInventory() != null ? profile.getInventory().getId() : null, Types.INTEGER);
                statement.setTimestamp(7, Timestamp.from(Instant.now()));
                statement.setLong(8, profile.getVersion() + 1);
                statement.setInt(9, profile.getId());
                statement.executeUpdate();
            }
        }
    }

    private <T> void resolveResult(Consumer<AsyncResult<T>> callback, T result) {
        Bukkit.getScheduler().runTask(loginSecurity, () ->
                callback.accept(new AsyncResult<T>(true, result, null)));
    }

    private <T> void resolveError(Consumer<AsyncResult<T>> callback, Exception error) {
        Bukkit.getScheduler().runTask(loginSecurity, () ->
                callback.accept(new AsyncResult<T>(false, null, error)));
    }
}
