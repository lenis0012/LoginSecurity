package com.lenis0012.bukkit.loginsecurity.modules.language;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class LanguageAPI {
    private static final String API_BASEPOINT = "http://lang.lenis0012.com";
    private static final String API_LIST = "/list";
    private static final String API_LANGUAGE = "/language/%s";
    private final List<Language> languages = Lists.newArrayList();
    private final JsonParser parser = new JsonParser();

    public List<Language> getLanguages() throws IOException {
        if(!languages.isEmpty()) {
            return Collections.unmodifiableList(languages);
        }

        JsonObject response = apiRequest(API_LIST);
        JsonArray languages = response.get("languages").getAsJsonArray();
        for(int i = 0; i < languages.size(); i++) {
            JsonObject json = languages.get(i).getAsJsonObject();
            Language language = new Language(json);
            this.languages.add(language);
        }
        Collections.sort(this.languages, new Comparator<Language>() {
            @Override
            public int compare(Language o1, Language o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return Collections.unmodifiableList(this.languages);
    }

    public Translation getTranslation(String code, Translation fallback) throws IOException {
        JsonObject response = apiRequest(API_LANGUAGE, code);
        if(!response.get("success").getAsBoolean()) {
            throw new IOException(response.get("error").getAsString());
        }
        return new Translation(fallback, response.get("data").getAsJsonObject(), code);
    }

    private JsonObject apiRequest(String endpoint, Object... parameters) throws IOException {
        endpoint = String.format(endpoint, parameters);
        URL url = new URL(API_BASEPOINT + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return parser.parse(builder.toString()).getAsJsonObject();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }
    }

    public static class Language {
        private final String code;
        private final String name;
        private final String authors;
        private final String pluginVersion;
        private final Date updatedAt;

        public Language(JsonObject json) {
            this.code = json.get("code").getAsString();
            this.name = json.get("localizedName").getAsString();
            this.authors = json.get("authors").getAsString();
            this.pluginVersion = json.get("pluginVersion").getAsString();
            this.updatedAt = new Date(json.get("updatedAt").getAsLong());
        }
    }
}
