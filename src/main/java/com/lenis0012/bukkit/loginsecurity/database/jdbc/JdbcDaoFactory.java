package com.lenis0012.bukkit.loginsecurity.database.jdbc;

import com.lenis0012.bukkit.loginsecurity.database.DaoFactory;
import com.lenis0012.bukkit.loginsecurity.database.ProfileDao;

import java.sql.Connection;

public class JdbcDaoFactory extends DaoFactory {

    @Override
    public ProfileDao getProfileDao() {
        return null;
    }

    public Connection getConnection() {
        return null;
    }
}
