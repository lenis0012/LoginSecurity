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
    @ConfigKey(path="password-required")
    private boolean passwordRequired = true;

    @ConfigHeader({
            "Available algorithms: BCRYPT, SCRYPT, PBKDF2, SHA3_256, WHIRLPOOL, ARGON2",
            "Check wiki for additional parameters."
    })
    @ConfigKey(path="hashing.algorithm")
    private String hashingAlgorithm = "BCRYPT";

    @ConfigKey
    private boolean blindness = true;

    @ConfigHeader({
            "Temporarily login location until player has logged in.",
            "Available options: DEFAULT, SPAWN, RANDOM"
    })
    @ConfigKey
    private String location = "DEFAULT";

    @ConfigHeader("Session timeout, set to -1 to disable.")
    @ConfigKey(path="session-timeout")
    private int sessionTimeout = 60;

    @ConfigHeader("Login timeout, set to -1 to disable.")
    @ConfigKey(path="login-timeout")
    private int loginTimeout = 60;

    @ConfigKey(path="max-login-tries")
    private int maxLoginTries = 5;

    public LoginSecurityConfig(ConfigurationModule module) {
        super(module);
    }

    public Algorithm getHashingAlgorithm() {
        return Algorithm.valueOf(hashingAlgorithm.toUpperCase());
    }
}
