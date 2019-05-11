package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerInventory;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerProfile;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class InventoryRepository {
    private final LoginSecurity loginSecurity;
    private final DataSource dataSource;

    public InventoryRepository(LoginSecurity loginSecurity, DataSource dataSource) {
        this.loginSecurity = loginSecurity;
        this.dataSource = dataSource;
    }

    public void insert(PlayerProfile profile, PlayerInventory inventory, Consumer<AsyncResult<PlayerInventory>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                insertBlocking(profile, inventory);
                resolveResult(callback, inventory);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void insertBlocking(PlayerProfile profile, PlayerInventory inventory) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ls_inventories(helmet, chestplate, leggings, boots, off_hand, contents) VALUES (?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS)) {

                prepareInsert(statement, inventory);
                statement.executeUpdate();

                try(ResultSet keys = statement.getGeneratedKeys()) {
                    if(!keys.next()) {
                        throw new RuntimeException("No keys were returned after insert");
                    }
                    inventory.setId(keys.getInt(1));
                }
            }
            profile.setInventoryId(inventory.getId());
            try(PreparedStatement statement = connection.prepareStatement(
                    "UPDATE ls_players SET inventory_id=? WHERE id=?;")) {
                statement.setInt(1, inventory.getId());
                statement.setInt(2, profile.getId());
                if(statement.executeUpdate() < 1) {
                    loginSecurity.getLogger().log(Level.WARNING, "Failed to set location id in profile");
                    throw new SQLException("Failed set location id in profile");
                }
            }
        }
    }

    public void findById(int id, Consumer<AsyncResult<PlayerInventory>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                final PlayerInventory inventory = findByIdBlocking(id);
                resolveResult(callback, inventory);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public PlayerInventory findByIdBlocking(int id) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ls_inventories WHERE id=?;")) {
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

    public void iterateAllBlocking(SQLConsumer<PlayerInventory> consumer) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet result = statement.executeQuery("SELECT * FROM ls_inventories;")) {
                    while(result.next()) {
                        consumer.accept(parseResultSet(result));
                    }
                }
            }
        }
    }

    public void batchInsert(SQLConsumer<SQLConsumer<PlayerInventory>> callback) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO ls_inventories(helmet, chestplate, leggings, boots, off_hand, contents) VALUES (?,?,?,?,?,?);")) {
                final AtomicInteger currentBatchSize = new AtomicInteger();
                callback.accept(inventory -> {
                    prepareInsert(statement, inventory);
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

    private void prepareInsert(PreparedStatement statement, PlayerInventory inventory) throws SQLException {
        statement.setString(1, inventory.getHelmet());
        statement.setString(2, inventory.getChestplate());
        statement.setString(3, inventory.getLeggings());
        statement.setString(4, inventory.getBoots());
        statement.setString(5, inventory.getOffHand());
        statement.setString(6, inventory.getContents());
    }

    private PlayerInventory parseResultSet(ResultSet result) throws SQLException {
        final PlayerInventory inventory = new PlayerInventory();
        inventory.setId(result.getInt("id"));
        inventory.setHelmet(result.getString("helmet"));
        inventory.setChestplate(result.getString("chestplate"));
        inventory.setLeggings(result.getString("leggings"));
        inventory.setBoots(result.getString("boots"));
        inventory.setOffHand(result.getString("off_hand"));
        inventory.setContents(result.getString("contents"));
        return inventory;
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
