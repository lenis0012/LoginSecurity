package com.lenis0012.bukkit.loginsecurity.database.jdbc;

import com.lenis0012.bukkit.loginsecurity.database.DaoFactory;
import com.lenis0012.bukkit.loginsecurity.database.ProfileDao;

import java.util.logging.Logger;

public class JdbcDaoFactory extends DaoFactory {
    private final Logger logger;
    private final JdbcConnectionPool connectionPool;

    private ProfileDao profileDao;

    public JdbcDaoFactory(Logger logger, JdbcConnectionPool connectionPool) {
        this.logger = logger;
        this.connectionPool = connectionPool;
    }

    @Override
    public ProfileDao getProfileDao() {
        if(profileDao == null) {
            this.profileDao = new JdbcProfileDao(connectionPool, logger);
        }
        return profileDao;
    }
}
