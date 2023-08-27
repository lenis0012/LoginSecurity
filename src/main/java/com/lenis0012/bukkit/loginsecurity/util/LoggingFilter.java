package com.lenis0012.bukkit.loginsecurity.util;

import com.lenis0012.bukkit.loginsecurity.LoginSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LoggingFilter extends AbstractFilter {
    private static final List<String> filteredWords = Arrays.asList("/register", "/login", "/changepassword", "/changepass");

    private final LoginSecurityConfig loginSecurityConfig;

    Result denyIfExposesPassword(String message) {
        if(message == null) {
            return Result.NEUTRAL;
        }

        message = message.toLowerCase();
        for(String word : filteredWords) {
            if(message.startsWith(word) || message.contains("issued server command: " + word)) {
                return Result.DENY;
            }
        }

        if(loginSecurityConfig.isUseCommandShortcut()) {
            final String loginShortcut = loginSecurityConfig.getLoginCommandShortcut().trim() + ' ';
            final String registerShortcut = loginSecurityConfig.getRegisterCommandShortcut().trim() + ' ';
            if(message.startsWith(loginShortcut)
                    || message.contains("issued server command: " + loginShortcut)
                    || message.startsWith(registerShortcut)
                    || message.contains("issued server command: " + registerShortcut)) {
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        return denyIfExposesPassword(event.getMessage().getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return denyIfExposesPassword(msg.getFormattedMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return denyIfExposesPassword(msg.toString());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return denyIfExposesPassword(msg);
    }
}
