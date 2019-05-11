package com.lenis0012.bukkit.loginsecurity.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer<T> {
    void accept(T entry) throws SQLException;
}
