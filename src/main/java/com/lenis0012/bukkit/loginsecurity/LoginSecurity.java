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

package com.lenis0012.bukkit.loginsecurity;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.Lists;
import com.lenis0012.bukkit.loginsecurity.modules.captcha.CaptchaManager;
import com.lenis0012.bukkit.loginsecurity.modules.general.GeneralModule;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;
import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageModule;
import com.lenis0012.bukkit.loginsecurity.modules.language.TranslatedMessage;
import com.lenis0012.bukkit.loginsecurity.modules.migration.MigrationModule;
import com.lenis0012.bukkit.loginsecurity.modules.storage.StorageModule;
import com.lenis0012.bukkit.loginsecurity.modules.threading.ThreadingModule;
import com.lenis0012.bukkit.loginsecurity.session.SessionManager;
import com.lenis0012.bukkit.loginsecurity.storage.*;
import com.lenis0012.bukkit.loginsecurity.util.LoggingFilter;
import com.lenis0012.pluginutils.PluginHolder;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class LoginSecurity extends PluginHolder {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Get the executor LoginSecurity uses for async processing.
     *
     * @return Executor service.
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get session manager.
     *
     * @return SessionManager
     */
    public static SessionManager getSessionManager() {
        return ((LoginSecurity) getInstance()).sessionManager;
    }

    /**
     * Get configuration settings.
     *
     * @return Configuration
     */
    public static LoginSecurityConfig getConfiguration() {
        return ((LoginSecurity) getInstance()).config();
    }

    /**
     * Translate a message by key.
     *
     * @param key Key to translate
     * @return Translated version by key and selected language
     */
    public static TranslatedMessage translate(LanguageKeys key) {
        return getInstance().getModule(LanguageModule.class).translate(key);
    }

    /**
     * Translate a message by key.
     *
     * @param key Key to translate
     * @return Translated version by key and selected language
     */
    public static TranslatedMessage translate(String key) {
        return getInstance().getModule(LanguageModule.class).translate(key);
    }

    private LoginSecurityConfig config;
    private SessionManager sessionManager;

    public LoginSecurity() {
        super(ConfigurationModule.class);
    }

    @Override
    public void enable() {
        // Load config
        ConfigurationModule module = getModule(ConfigurationModule.class);
        this.config = module.createCustomConfig(LoginSecurityConfig.class);
        config.reload();
        config.save();

        // Load session manager
        this.sessionManager = new SessionManager();

        // Filter log
        org.apache.logging.log4j.core.Logger consoleLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        consoleLogger.addFilter(new LoggingFilter());

        // Register modules
        registry.registerModules(
                LanguageModule.class,
                StorageModule.class,
                MigrationModule.class,
                GeneralModule.class,
                ThreadingModule.class,
                CaptchaManager.class);
    }

    @Override
    public void disable() {
        // Wait for all async processes to complete...
        getLogger().log(Level.INFO, "Waiting for queued tasks...");
        executorService.shutdown();
        getLogger().log(Level.INFO, "ExecutorService shut down, ready to disable.");
    }

    public LoginSecurityConfig config() {
        return config;
    }

    @Override
    public EbeanServer getDatabase() {
        return super.getDatabase();
    }
}
