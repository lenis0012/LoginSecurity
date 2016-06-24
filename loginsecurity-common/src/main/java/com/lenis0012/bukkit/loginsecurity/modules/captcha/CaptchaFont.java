package com.lenis0012.bukkit.loginsecurity.modules.captcha;

import org.bukkit.map.MapFont;

public class CaptchaFont extends MapFont {
    private static CaptchaFont instance = new CaptchaFont();

    public static CaptchaFont getInstance() {
        return instance;
    }

    private final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final long[] glyphs = new long[] {
            0x0C1E33333F333300L, // A
            0x3F66663E66663F00L, // B
            0x3C66030303663C00L, // C
            0x1F36666666361F00L, // D
            0x7F46161E16467F00L, // E
            0x7F46161E16060F00L, // F
            0x3C66030373667C00L, // G
            0x3333333F33333300L, // H
            0x1E0C0C0C0C0C1E00L, // I
            0x7830303033331E00L, // J
            0x6766361E36666700L, // K
            0x0F06060646667F00L, // L
            0x63777F7F6B636300L, // M
            0x63676F7B73636300L, // N
            0x1C36636363361C00L, // O
            0x3F66663E06060F00L, // P
            0x1E3333333B1E3800L, // Q
            0x3F66663E36666700L, // R
            0x1E33070E38331E00L, // S
            0x3F2D0C0C0C0C1E00L, // T
            0x3333333333333F00L, // U
            0x33333333331E0C00L, // V
            0x6363636B7F776300L, // W
            0x6363361C1C366300L, // X
            0x3333331E0C0C1E00L, // Y
            0x7F6331184C667F00L, // X
            0x3E63737B6F673E00L, // 0
            0x0C0E0C0C0C0C3F00L, // 1
            0x1E33301C06333F00L, // 2
            0x1E33301C30331E00L, // 3
            0x383C36337F307800L, // 4
            0x3F031F3030331E00L, // 5
            0x1C06031F33331E00L, // 6
            0x3F3330180C0C0C00L, // 7
            0x1E33331E33331E00L, // 8
            0x1E33333E30180E00L // 9
    };

    private CaptchaFont() {
        for(int i = 0; i < characters.length(); i++) {
            char character = characters.charAt(i);
            long glyph = glyphs[i];
            boolean[] data = new boolean[8 * 8];
            for(int j = 0; j < data.length; j++) {
                int pos = 63 - j;
                data[j] = ((glyph >> pos) & 1) == 1;
            }
            boolean[] bigger = new boolean[16 * 16];
            for(int j = 0; j < data.length; j++) {
                int row = j / 8;
                int col = 7 - (j % 8);
                bigger[row * 2 * 16 + col * 2] = data[j];
                bigger[(row * 2 + 1) * 16 + col * 2] = data[j];
                bigger[(row * 2 + 1) * 16 + col * 2 + 1] = data[j];
                bigger[row * 2 * 16 + col * 2 + 1] = data[j];
            }
            setChar(character, new CharacterSprite(16, 16, bigger));
        }
    }
}
