package com.lenis0012.bukkit.loginsecurity.database;

public abstract class DaoFactory {

    /**
     * Get profile data access object.
     * It handles profile storage.
     *
     * @return Profile DAO
     */
    public abstract ProfileDao getProfileDao();
}
