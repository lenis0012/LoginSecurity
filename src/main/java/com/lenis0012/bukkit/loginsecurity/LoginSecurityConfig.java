package com.lenis0012.bukkit.loginsecurity;

import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import com.lenis0012.pluginutils.modules.configuration.AbstractConfig;
import com.lenis0012.pluginutils.modules.configuration.ConfigurationModule;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigHeader;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigKey;
import com.lenis0012.pluginutils.modules.configuration.mapping.ConfigMapper;
import lombok.Getter;

@Getter
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

    @ConfigHeader("Session timeout, set to -1 to disable.")
    @ConfigKey
    private int sessionTimeout = 60;

    @ConfigHeader("Login timeout, set to -1 to disable.")
    @ConfigKey
    private int loginTimeout = 120;

    @ConfigHeader("Login/register message delay")
    @ConfigKey
    private int loginMessageDelay = 10;

    public LoginSecurityConfig(ConfigurationModule module) {
        super(module);
    }

    public Algorithm getHashingAlgorithm() {
        return Algorithm.valueOf(hashingAlgorithm.toUpperCase());
    }
}
