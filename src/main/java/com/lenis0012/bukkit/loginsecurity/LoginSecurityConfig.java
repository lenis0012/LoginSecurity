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

import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.pluginutils.modules.configuration.AbstractConfig;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigHeader;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigKey;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigMapper;
import lombok.Getter;

@ConfigMapper(fileName = "config.yml", header = {
        "LoginSecurity configuration.",
        "Some information is provided in the form of comments",
        "For more info visit https://github.com/lenis0012/LoginSecurity-2/wiki/Configuration"
})
public class LoginSecurityConfig extends AbstractConfig {
    /**
     * Registration settings
     */
    @ConfigKey(path="register.required")
    private boolean passwordRequired = true;
    @ConfigHeader("When enabled, users need to enter a captcha on registration.")
    @ConfigKey(path = "register.captcha")
    private boolean registerCaptcha = false;

    /**
     * Login settings
     */
    @ConfigKey(path="login.max-tries")
    private int maxLoginTries = 5;

    /**
     * Password settings.
     */
    @ConfigKey(path="password.min-length")
    private int passwordMinLength = 6;
    @ConfigKey(path="password-max-length")
    private int passwordMaxLength = 32;
    @ConfigHeader({
            "Available algorithms: BCRYPT, SCRYPT, PBKDF2, SHA3_256, WHIRLPOOL",
            "Check wiki for additional parameters."
    })
    @ConfigKey(path="password.hashing.algorithm")
    private String hashingAlgorithm = "BCRYPT";

    /**
     * Join settings.
     */
    @ConfigHeader("When enabled, player gets a blindness effect when not logged in.")
    @ConfigKey(path = "join.blindness")
    private boolean blindness = true;
    @ConfigHeader({
            "Temporarily login location until player has logged in.",
            "Available options: DEFAULT, SPAWN, RANDOM"
    })
    @ConfigKey(path = "join.location")
    private String location = "DEFAULT";
    @ConfigHeader({
            "Hides the player's inventory until they log in.",
            "The inventory is never lost, even after reboot or crash."
    })
    @ConfigKey(path="join.hide-inventory")
    private boolean hideInventory = true;

    /**
     * Username settings.
     */
    @ConfigHeader({"Remove special characters like @ and # from the username.",
            "Disabling this can be a security risk!"
    })
    @ConfigKey(path="username.filter-special-chars")
    private boolean filterSpecialChars = true;
    @ConfigKey(path="username.min-length")
    private int usernameMinLength = 3;
    @ConfigKey(path="username.max-length")
    private int usernameMaxLength = 16;

    @ConfigHeader(path="command-shortcut", value="Shortcut that can be used as alternative to login/register command. Does not replace the defaults")
    @ConfigKey(path="command-shortcut.enabled")
    private boolean useCommandShortcut = false;
    @ConfigKey(path="command-shortcut.login")
    private String loginCommandShortcut = "/l";
    @ConfigKey(path="command-shortcut.register")
    private String registerCommandShortcut = "/reg";

    @ConfigKey(path = "updater.enabled")
    private boolean updaterEnabled = true;
    @ConfigHeader("The type of builds you are checking. RELEASE, BETA, ALPHA")
    @ConfigKey(path = "updater.channel")
    private String updaterChannel = "BETA";

    @ConfigHeader("Session timeout, set to -1 to disable.")
    @ConfigKey
    private int sessionTimeout = 60;

    @ConfigHeader("Login timeout, set to -1 to disable.")
    @ConfigKey
    private int loginTimeout = 120;

    @ConfigHeader("Login/register message delay.")
    @ConfigKey
    private int loginMessageDelay = 10;

    @ConfigHeader({
            "Language for messages, check wiki for more info.",
            "List: http://lang.lenis0012.com/list",
            "Note: Changing this setting will request info from lang.lenis0012.com!"
    })
    @ConfigKey
    private String language = "en_us";

    public LoginSecurityConfig(ConfigurationModule module) {
        super(module);
    }

    public Algorithm getHashingAlgorithm() {
        return Algorithm.valueOf(hashingAlgorithm.toUpperCase());
    }

    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    public boolean isRegisterCaptcha() {
        return registerCaptcha;
    }

    public int getMaxLoginTries() {
        return maxLoginTries;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public int getPasswordMaxLength() {
        return passwordMaxLength;
    }

    public boolean isBlindness() {
        return blindness;
    }

    public String getLocation() {
        return location;
    }

    public boolean isHideInventory() {
        return hideInventory;
    }

    public boolean isFilterSpecialChars() {
        return filterSpecialChars;
    }

    public int getUsernameMinLength() {
        return usernameMinLength;
    }

    public int getUsernameMaxLength() {
        return usernameMaxLength;
    }

    public boolean isUseCommandShortcut() {
        return useCommandShortcut;
    }

    public String getLoginCommandShortcut() {
        return loginCommandShortcut;
    }

    public String getRegisterCommandShortcut() {
        return registerCommandShortcut;
    }

    public boolean isUpdaterEnabled() {
        return updaterEnabled;
    }

    public String getUpdaterChannel() {
        return updaterChannel;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public int getLoginMessageDelay() {
        return loginMessageDelay;
    }

    public String getLanguage() {
        return language;
    }
}
