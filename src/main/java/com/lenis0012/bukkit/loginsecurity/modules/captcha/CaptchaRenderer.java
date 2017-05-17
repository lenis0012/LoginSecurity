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

package com.lenis0012.bukkit.loginsecurity.modules.captcha;

import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public class CaptchaRenderer extends MapRenderer {

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if(MetaData.has(player, "ls_captcha_set") || !MetaData.has(player, "ls_captcha_value")) {
            return;
        }

        final String text = MetaData.get(player, "ls_captcha_value", String.class);
        MetaData.set(player, "ls_captcha_set", true);
        System.out.println("text: " + text);

        // Clear map
        for(int x = 0; x < 128; x++) {
            for(int y = 0; y < 128; y++) {
                canvas.setPixel(x, y, (byte) 0); //
            }
        }

        // Draw captcha
        final int y = 128 / 2 - CaptchaFont.getInstance().getHeight() / 2;
        final int x = 128 / 2 - CaptchaFont.getInstance().getWidth(text) / 2;
        canvas.drawText(x, y, CaptchaFont.getInstance(), text);
    }
}
