/*
 * This file is a part of LoginSecurity.
 *
 * Copyright (c) 2017 Lennart ten Wolde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
