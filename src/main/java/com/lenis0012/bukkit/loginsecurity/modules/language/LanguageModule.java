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
            try {
                this.translation = languageAPI.getTranslation(languageCode, base);
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

    public Translation getTranslation() {
        return translation;
    }

    public TranslatedMessage translate(LanguageKeys key) {
        return translate(key.toString());
    }

    public TranslatedMessage translate(String rawKey) {
        return translation.translate(rawKey);
    }
}
