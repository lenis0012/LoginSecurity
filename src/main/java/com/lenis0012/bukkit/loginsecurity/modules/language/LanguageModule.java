package com.lenis0012.bukkit.loginsecurity.modules.language;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.pluginutils.Module;

import java.io.*;
import java.util.logging.Level;

public class LanguageModule extends Module<LoginSecurity> {
    private Translation translation;
    private LanguageAPI languageAPI;

    public LanguageModule(LoginSecurity plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        logger().log(Level.INFO, "Loading base translations from \"en_us\"");
        Translation base = byResource("en_us", null);

        this.languageAPI = new LanguageAPI();
        String languageCode = LoginSecurity.getConfiguration().getLanguage();
        logger().log(Level.INFO, "Loading specified translations from \"" + languageCode + "\"");
        File file = new File(plugin.getDataFolder(), languageCode + ".json");
        if(languageCode.equalsIgnoreCase("en_us") || languageCode.equalsIgnoreCase("default")) {
            // Use built-in
            this.translation = base;
        } else if(file.exists()) {
            // Use local file
            this.translation = byFile(file, base);
        } else {
            logger().log(Level.INFO, "FUCK ME");
            try {
                this.translation = languageAPI.getTranslation(languageCode, base);
                logger().log(Level.INFO, "FUCK ME TWICE");
            } catch (IOException e) {
                logger().log(Level.WARNING, "Couldn't get translation, defaulting to en_us", e);
                this.translation = base;
            }
        }
    }

    @Override
    public void disable() {
    }

    private Translation byFile(File file, Translation fallback) {
        String name = file.getName().split("\\.")[0];
        try {
            InputStream input = new FileInputStream(file);
            return new Translation(fallback, new InputStreamReader(input), name);
        } catch(IOException e) {
            throw new RuntimeException("Couldn't read internal language file", e);
        }
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
