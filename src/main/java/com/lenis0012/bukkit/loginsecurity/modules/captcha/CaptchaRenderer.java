package com.lenis0012.bukkit.loginsecurity.modules.captcha;

import com.lenis0012.bukkit.loginsecurity.util.MetaData;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CaptchaRenderer extends MapRenderer {

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if(MetaData.has(player, "ls_captcha_set") || !MetaData.has(player, "ls_captcha_value")) {
            return;
        }

        final String text = MetaData.get(player, "ls_captcha_value", String.class);
        MetaData.set(player, "ls_captcha_set", true);

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
