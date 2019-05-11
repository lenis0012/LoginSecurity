package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerLocation;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class LocationRepository {
    private final LoginSecurity loginSecurity;
    private final DataSource dataSource;

    public LocationRepository(LoginSecurity loginSecurity, DataSource dataSource) {
        this.loginSecurity = loginSecurity;
        this.dataSource = dataSource;
    }

    public void insertLoginLocation(PlayerProfile profile, PlayerLocation location, Consumer<AsyncResult<PlayerLocation>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                insertLoginLocationBlocking(profile, location);
                resolveResult(callback, location);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void insertLoginLocationBlocking(PlayerProfile profile, PlayerLocation location) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ls_locations(world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS)) {
                prepareInsert(statement, location);
                statement.executeUpdate();

                try(ResultSet keys = statement.getGeneratedKeys()) {
                    if(!keys.next()) {
                        throw new SQLException("Could not get ID for new location");
                    }

                    location.setId(keys.getInt(1));
                }
            }
            profile.setLoginLocationId(location.getId());
            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE ls_players SET location_id=? WHERE id=?;")) {
                statement.setInt(1, location.getId());
                statement.setInt(2, profile.getId());
                if(statement.executeUpdate() < 1) {
                    loginSecurity.getLogger().log(Level.WARNING, "Failed to set inventory id in profile");
                    throw new SQLException("Failed set location id in profile");
                }
            }
        }
    }

    public void findById(int id, Consumer<AsyncResult<PlayerLocation>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                final PlayerLocation location = findByIdBlocking(id);
                resolveResult(callback, location);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public PlayerLocation findByIdBlocking(int id) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ls_locations WHERE id=?;")) {
                statement.setInt(1, id);
                try(ResultSet result = statement.executeQuery()) {
                    if(!result.next()) {
                        return null; // Not found
                    }

                    return parseResultSet(result);
                }
            }
        }
    }

    public void iterateAllBlocking(SQLConsumer<PlayerLocation> consumer) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet result = statement.executeQuery("SELECT * FROM ls_locations;")) {
                    while(result.next()) {
                        consumer.accept(parseResultSet(result));
                    }
                }
            }
        }
    }

    public void batchInsert(SQLConsumer<SQLConsumer<PlayerLocation>> callback) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_locations(world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?);")) {
                final AtomicInteger currentBatchSize = new AtomicInteger();
                callback.accept(location -> {
                    prepareInsert(statement, location);
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

    private PlayerLocation parseResultSet(ResultSet result) throws SQLException {
        PlayerLocation location = new PlayerLocation();
        location.setId(result.getInt("id"));
        location.setWorld(result.getString("world"));
        location.setX(result.getDouble("x"));
        location.setY(result.getDouble("y"));
        location.setZ(result.getDouble("z"));
        location.setYaw(result.getInt("yaw"));
        location.setPitch(result.getInt("pitch"));
        return location;
    }

    private void prepareInsert(PreparedStatement statement, PlayerLocation location) throws SQLException {
        statement.setString(1, location.getWorld());
        statement.setDouble(2, location.getX());
        statement.setDouble(3, location.getY());
        statement.setDouble(4, location.getZ());
        statement.setInt(5, location.getYaw());
        statement.setInt(6, location.getPitch());
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
