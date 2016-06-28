package com.lenis0012.bukkit.loginsecurity.modules.language;

public class TranslatedMessage {
    private String message;

    public TranslatedMessage(String message) {
        this.message = message;
    }

    public TranslatedMessage param(String key, String value) {
        message = message.replace("%" + key + "%", value);
        return this;
    }

    @Override
    public String toString() {
        return message;
    }
}
