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
    private final Plugin plugin;
    private final ConnectionPoolDataSource dataSource;
    private final ReentrantLock lock;

    private final int timeout;
    private final long maxLifetime;

    private volatile PooledConnection pooledConnection;
    private BukkitTask recreateTask;
    private boolean closing = false;

    public SingleConnectionDataSource(Plugin plugin, ConnectionPoolDataSource dataSource) throws SQLException {
        this(plugin, dataSource, 5000, TimeUnit.SECONDS.toMillis(30));
    }

    public SingleConnectionDataSource(Plugin plugin, ConnectionPoolDataSource dataSource, int timeout, long maxLifetime) throws SQLException {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.lock = new ReentrantLock(true);

        this.maxLifetime = maxLifetime;
        this.timeout = timeout;

        createConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(closing) throw new SQLException("Database is shutting down");
        lock.lock();
        try {
            if(pooledConnection != null) {
                final Connection connection = pooledConnection.getConnection();
                if(!connection.isClosed()) {
                    if(!connection.isValid(timeout)) {
                        tryClose(pooledConnection);
                    } else {
                        return connection;
                    }
                }
            }

            createConnection();
            return pooledConnection.getConnection();
        } catch (Throwable t) {
            lock.unlock();
            throw t;
        }
    }

    private void createConnection() throws SQLException {
        if(recreateTask != null) recreateTask.cancel();
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
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void tryClose(PooledConnection connection) {
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
        }
    }

    @Override
    public void connectionClosed(ConnectionEvent event) {
//        LoginSecurity.getInstance().getLogger().log(Level.INFO, "Returning connection " + event.getSource().getClass().getSimpleName());
//        Thread.dumpStack();
        lock.unlock();
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent event) {
        PooledConnection brokenConnection = this.pooledConnection;
        this.pooledConnection = null;
        lock.unlock();

        tryClose(brokenConnection);
    }
}
