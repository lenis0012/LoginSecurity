/*
 * Licensed under the MIT License (MIT).
 *
 * Copyright 2017 Lennart ten Wolde
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lenis0012.bukkit.loginsecurity.database.jdbc;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple lightweight connection pool.
 * This connection pool manages connections, allowing for a specific amount of connections to be consumed.
 * The connections are recycled when the consumer {@link Connection#close() closes} the connection.
 *
 * @author Lennart ten Wolde
 */
public class JdbcConnectionPool {
    // Configuration and global variables
    private final ConnectionPoolDataSource dataSource;
    private final ConnectionEventListener eventListener;
    private final Semaphore semaphore;

    // Connections in this that are currently in use or idle
    private final Set<PooledConnection> activeConnections = ConcurrentHashMap.newKeySet();
    private final Queue<PooledConnection> recycledConnections = new ConcurrentLinkedQueue<>();

    // Statistics
    private final AtomicInteger connectionsCreated = new AtomicInteger();
    private final AtomicInteger connectionsProvided = new AtomicInteger();
    private final AtomicInteger errorsCaught = new AtomicInteger();

    /**
     * Create a new connection pool.
     *
     * @param dataSource The datasource which allocates connections.
     * @param maxConnections The maximum amount of connections which may be consumed at once.
     */
    public JdbcConnectionPool(ConnectionPoolDataSource dataSource, int maxConnections) {
        this.dataSource = dataSource;
        this.eventListener = new JdbcConnectionEventListener(this);

        // Using a fair semaphore to make sure that sql queries are executed in the right order
        this.semaphore = new Semaphore(maxConnections, true);
    }

    /**
     * Get a new connection from the pool.
     * This connection may either be a new, or recycled connection.
     * If not validated, an invalid recycled connection may be returned.
     *
     * @param validate Whether or not the connection should be validated if recycled
     * @return A connection from the pool, recycled or new.
     * @throws SQLException Database access error
     */
    public Connection getConnection(boolean validate) throws SQLException {
        try {
            semaphore.acquire();

            // Try to recycle a connection
            PooledConnection pooledConnection;
            while((pooledConnection = recycledConnections.poll()) != null && validate) {
                try {
                    Connection connection = pooledConnection.getConnection();
                    if (connection.isValid(1000)) {
                        break;
                    } else {
                        closePooledConnection(pooledConnection);
                    }
                } catch(SQLException e) {
                    closePooledConnection(pooledConnection);
                }
            }

            // If we couldn't get a valid recycled connection, create a new one
            if(pooledConnection == null) {
                pooledConnection = createConnection();
            }

            // Mark connection as claimed and return
            activeConnections.add(pooledConnection);
            connectionsProvided.incrementAndGet();
            return pooledConnection.getConnection();
        } catch (InterruptedException e) {
            return null; // Some greater force stopped us
        }
    }

    /**
     * Get the total amount of connections that were created by this pool.
     *
     * @return Amount of connections created
     */
    public int getConnectionsCreated() {
        return connectionsCreated.get();
    }

    /**
     * Get the total amount of times a connection was provided by this pool,
     * regardless of whether or not it was recycled.
     *
     * @return Amount of times a connection was provided
     */
    public int getConnectionsProvided() {
        return connectionsProvided.get();
    }

    /**
     * Get the total amount of errors that were caught by this pool in connections that it served.
     *
     * @return Amount of errors caught
     */
    public int getErrorsCaught() {
        return errorsCaught.get();
    }

    private PooledConnection createConnection() throws SQLException {
        // Create a new reusable connection
        PooledConnection pooledConnection = dataSource.getPooledConnection();
        pooledConnection.addConnectionEventListener(eventListener);
        connectionsCreated.incrementAndGet();
        return pooledConnection;
    }

    private void returnConnection(PooledConnection connection) {
        // Make sure this connection is still active
        if(activeConnections.remove(connection)) {
            // Recycle connection and allow next consumer
            recycledConnections.offer(connection);
            semaphore.release();
        }
    }

    private void returnBrokenConnection(PooledConnection connection) {
        // Remove all references to this connection
        recycledConnections.remove(connection);
        if(activeConnections.remove(connection)) {
            // If this connection was active, allow next consumer while we deal with the broken one
            semaphore.release();
        }

        errorsCaught.incrementAndGet();
        closePooledConnection(connection);
    }

    private void closePooledConnection(PooledConnection pooledConnection) {
        try {
            pooledConnection.close();
        } catch (SQLException e) {
            // We don't care if it failed.
        }
    }

    private static class JdbcConnectionEventListener implements ConnectionEventListener {
        private final JdbcConnectionPool connectionPool;

        private JdbcConnectionEventListener(JdbcConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
        }

        @Override
        public void connectionClosed(ConnectionEvent event) {
            // Consumer is finished with connection, return to pool.
            PooledConnection connection = (PooledConnection) event.getSource();
            connectionPool.returnConnection(connection);
        }

        @Override
        public void connectionErrorOccurred(ConnectionEvent event) {
            // Error occurred while consumer was working with connection, we inform the pool.
            PooledConnection connection = (PooledConnection) event.getSource();
            connectionPool.returnBrokenConnection(connection);
        }
    }
}
