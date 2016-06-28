package com.lenis0012.bukkit.loginsecurity.modules.language;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.pluginutils.Module;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LanguageModule extends Module<LoginSecurity> {
    private Translation translation;

    public LanguageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        logger().log(Level.INFO, "Loading base translations from \"en_us\"");
        Translation base = byResource("en_us", null);

        // TODO: Actually load from config
        logger().log(Level.INFO, "Loading specified translations from \"en_us\"");
        this.translation = base;
    }

    @Override
    public void disable() {
    }

    private Translation byResource(String name, Translation fallback) {
        try {
            InputStream input = plugin.getResource("lang/" + name + ".json");
            return new Translation(fallback, new InputStreamReader(input), name);
        } catch(IOException e) {
            throw new RuntimeException("Couldn't read internal language file", e);
        }
    }

    public TranslatedMessage translate(LanguageKeys key) {
        return translate(key.toString());
    }

    public TranslatedMessage translate(String rawKey) {
        return translation.translate(rawKey);
    }
}
