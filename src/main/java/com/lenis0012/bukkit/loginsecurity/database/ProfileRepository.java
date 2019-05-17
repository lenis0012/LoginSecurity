package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import com.lenis0012.bukkit.loginsecurity.util.UserIdMode;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
                prepareInsert(statement, profile);
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

                prepareUpdate(statement, profile);
                statement.executeUpdate();
            }
        }
    }

    public void delete(PlayerProfile profile, Consumer<AsyncResult<PlayerProfile>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                deleteBlocking(profile);
                resolveResult(callback, profile);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void deleteBlocking(PlayerProfile profile) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM ls_players WHERE id=?;")) {
                statement.setInt(1, profile.getId());
                statement.executeUpdate();
            }
        }
    }

    public void findByUniqueId(UUID unqiueId, Consumer<AsyncResult<PlayerProfile>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                final PlayerProfile profile = findByUniqueUserIdBlocking(unqiueId);
                resolveResult(callback, profile);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public PlayerProfile findByUniqueUserIdBlocking(UUID uniqueId) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ls_players WHERE unique_user_id=?")) {
                statement.setString(1, uniqueId.toString());
                try(ResultSet result = statement.executeQuery()) {
                    if(!result.next()) {
                        return null; // Not found
                    }

                    return parseResultSet(result);
                }
            }
        }
    }

    public void findByLastName(String lastName, Consumer<AsyncResult<PlayerProfile>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                final PlayerProfile profile = findByLastNameBlocking(lastName);
                resolveResult(callback, profile);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public PlayerProfile findByLastNameBlocking(String lastName) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ls_players WHERE last_name=?;")) {
                statement.setString(1, lastName);
                try(ResultSet result = statement.executeQuery()) {
                    if(!result.next()) {
                        return null;
                    }

                    return parseResultSet(result);
                }
            }
        }
    }

    public void iterateAllBlocking(SQLConsumer<PlayerProfile> consumer) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet result = statement.executeQuery("SELECT * FROM ls_players;")) {
                    while(result.next()) {
                        consumer.accept(parseResultSet(result));
                    }
                }
            }
        }
    }

    public void batchInsert(SQLConsumer<SQLConsumer<PlayerProfile>> callback) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_players(uuid_mode,unique_user_id,last_name,ip_address,password,hashing_algorithm,location_id,inventory_id,last_login,registration_date,optlock) VALUES(?,?,?,?,?,?,?,?,?,?,?);")) {
                final AtomicInteger currentBatchSize = new AtomicInteger();
                callback.accept(profile -> {
                    prepareInsert(statement, profile);
                    statement.addBatch();

                    // execute batch if size is >= BATCH_SIZE
                    if(currentBatchSize.incrementAndGet() >= LoginSecurityDatabase.BATCH_SIZE) {
                        statement.executeBatch();
                        currentBatchSize.set(0);
                    }
                });
                // execute batch
                if(currentBatchSize.get() > 0) statement.executeBatch();
            }
        }
    }

    private void prepareInsert(PreparedStatement statement, PlayerProfile profile) throws SQLException {
        statement.setString(1, profile.getUniqueIdMode().getId());
        statement.setString(2, profile.getUniqueUserId());
        statement.setString(3, profile.getLastName());
        statement.setString(4, profile.getIpAddress());
        statement.setString(5, profile.getPassword());
        statement.setInt(6, profile.getHashingAlgorithm());

        if(profile.getLoginLocationId() == null) statement.setNull(7, Types.INTEGER);
        else statement.setInt(7, profile.getLoginLocationId());
        if(profile.getInventoryId() == null) statement.setNull(8, Types.INTEGER);
        else statement.setInt(8, profile.getInventoryId());

        statement.setTimestamp(9, Timestamp.from(Instant.now()));
        statement.setDate(10, new Date(System.currentTimeMillis()));
        statement.setLong(11, 1);
    }

    private void prepareUpdate(PreparedStatement statement, PlayerProfile profile) throws SQLException {
        statement.setString(1, profile.getLastName());
        statement.setString(2, profile.getIpAddress());
        statement.setString(3, profile.getPassword());
        statement.setInt(4, profile.getHashingAlgorithm());

        if(profile.getLoginLocationId() == null) statement.setNull(5, Types.INTEGER);
        else statement.setInt(5, profile.getLoginLocationId());
        if(profile.getInventoryId() == null) statement.setNull(6, Types.INTEGER);
        else statement.setInt(6, profile.getInventoryId());

        statement.setTimestamp(7, Timestamp.from(Instant.now()));
        statement.setLong(8, profile.getVersion() + 1);
        statement.setInt(9, profile.getId());
    }

    private PlayerProfile parseResultSet(ResultSet result) throws SQLException {
        PlayerProfile profile = new PlayerProfile();
        profile.setId(result.getInt("id"));
        profile.setUniqueIdMode(UserIdMode.fromId(result.getString("uuid_mode")));
        profile.setUniqueUserId(result.getString("unique_user_id"));
        profile.setLastName(result.getString("last_name"));
        profile.setIpAddress(result.getString("ip_address"));
        profile.setPassword(result.getString("password"));
        profile.setHashingAlgorithm(result.getInt("hashing_algorithm"));

        profile.setLoginLocationId((Integer) result.getObject("location_id"));
        profile.setInventoryId((Integer) result.getObject("inventory_id"));

        profile.setLastLogin(result.getTimestamp("last_login"));
        profile.setRegistrationDate(result.getDate("registration_date"));
        profile.setVersion(result.getLong("optlock"));
        return profile;
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
