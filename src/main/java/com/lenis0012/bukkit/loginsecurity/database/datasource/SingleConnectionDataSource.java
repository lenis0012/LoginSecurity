package com.lenis0012.bukkit.loginsecurity.database.datasource;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class SingleConnectionDataSource extends DataSourceAdapter implements ConnectionEventListener, Runnable {
    private static final int VALID_CHECK_BYPASS = 500; // 0.5 sec
    private static final int DEFAULT_TIMEOUT = 5000; // 5 sec
    private static final long DEFAULT_MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);

    private final Plugin plugin;
    private final ConnectionPoolDataSource dataSource;
    private final ReentrantLock lock;

    private final int timeout;
    private final long maxLifetime;

    private volatile PooledConnection pooledConnection;
    private volatile long lastUsedTime;
    private volatile boolean obtainingConnection;
    private BukkitTask recreateTask;
    private boolean closing = false;

    public SingleConnectionDataSource(Plugin plugin, ConnectionPoolDataSource dataSource) {
        this(plugin, dataSource, DEFAULT_TIMEOUT, DEFAULT_MAX_LIFETIME);
    }

    public SingleConnectionDataSource(Plugin plugin, ConnectionPoolDataSource dataSource, int timeout, long maxLifetime) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.lock = new ReentrantLock(true);

        this.maxLifetime = maxLifetime;
        this.timeout = timeout;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(closing) throw new SQLException("Database is shutting down");
        lock.lock();
        this.obtainingConnection = true;
        try {
            if(pooledConnection != null) {
                try {
                    final Connection connection = pooledConnection.getConnection();
                    if(!connection.isClosed()) {
                        if((lastUsedTime - System.currentTimeMillis() <= VALID_CHECK_BYPASS) || connection.isValid(timeout)) {
                            this.obtainingConnection = false;
//                            plugin.getLogger().log(Level.FINE, "Re-using connection");
                            return connection;
                        } else {
                            tryClose(pooledConnection);
                        }
                    }
                } catch(SQLException e) {
                    tryClose(pooledConnection);
                }
            }

            createConnection();
            Connection connection = pooledConnection.getConnection();
            this.obtainingConnection = false;
            return connection;
        } catch (Throwable t) {
            lock.unlock();
            throw t;
        }
    }

    public void createConnection() throws SQLException {
        if(recreateTask != null) recreateTask.cancel();
        if(pooledConnection != null) tryClose(pooledConnection);
        this.pooledConnection = dataSource.getPooledConnection();
        pooledConnection.addConnectionEventListener(this);
        this.recreateTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, maxLifetime / 50);
    }

    @Override
    public void run() {
        this.recreateTask = null;
        lock.lock();
        try {
            tryClose(pooledConnection);
            createConnection();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to recreate database connection", e);
        } finally {
            lock.unlock();
        }
    }

    private void tryClose(PooledConnection connection) {
        this.pooledConnection = null;
        if(connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
        }
    }

    public void shutdown() throws SQLException {
        this.closing = true;
        if(recreateTask != null) recreateTask.cancel();
        lock.lock();

        if(pooledConnection != null) {
            pooledConnection.close();
            this.pooledConnection = null;
        }
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
//        LoginSecurity.getInstance().getLogger().log(Level.INFO, "Returning connection " + event.getSource().getClass().getSimpleName());
//        Thread.dumpStack();
        this.lastUsedTime = System.currentTimeMillis();
        if(!obtainingConnection) lock.unlock();
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent event) {
        final PooledConnection brokenConnection = event.getSource() instanceof PooledConnection ?
                (PooledConnection) event.getSource() : pooledConnection;
        this.pooledConnection = null;
        if(!obtainingConnection) lock.unlock();

        tryClose(brokenConnection);
    }
}
