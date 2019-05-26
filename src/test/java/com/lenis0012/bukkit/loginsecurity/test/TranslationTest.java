package com.lenis0012.bukkit.loginsecurity.test;

import com.lenis0012.bukkit.loginsecurity.modules.language.LanguageKeys;
import com.lenis0012.bukkit.loginsecurity.modules.language.Translation;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class TranslationTest {

    @Test
    public void testLanguageKeys() throws Exception {
        InputStream languageResource = TranslationTest.class.getResourceAsStream("/lang/en_us.json");
        Translation translation = new Translation(null, new InputStreamReader(languageResource), "en_us");

        for(LanguageKeys key : LanguageKeys.values()) {
            Assert.assertNotNull("Invalid translation key", translation.translate(key.toString()));
        }
    }
}
