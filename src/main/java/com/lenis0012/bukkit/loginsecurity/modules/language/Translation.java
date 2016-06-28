package com.lenis0012.bukkit.loginsecurity.modules.language;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wrapper around a translation object.
 *
 * This can be any form of reader.
 */
public class Translation {
    private final Map<String, String> translations = Maps.newConcurrentMap();
    private Translation fallback;
    private String name;

    public Translation(Translation fallback, Reader reader, String name) throws IOException {
        this.fallback = fallback;
        JsonParser parser = new JsonParser();
        JsonObject data = parser.parse(reader).getAsJsonObject();
        for(Entry<String, JsonElement> entry : data.entrySet()) {
            translations.put(entry.getKey(), entry.getValue().getAsString());
        }
        this.name = name.split("\\.")[0];
    }

    /**
     * Get the translation for a key.
     *
     * @param key Key to translate
     * @return Translation message
     */
    public TranslatedMessage translate(String key) {
        if(translations.containsKey(key)) {
            return new TranslatedMessage(translations.get(key));
        } else if(fallback != null) {
            return fallback.translate(key);
        } else {
            throw new IllegalArgumentException("Unknown translation key for language " + name);
        }
    }
}
