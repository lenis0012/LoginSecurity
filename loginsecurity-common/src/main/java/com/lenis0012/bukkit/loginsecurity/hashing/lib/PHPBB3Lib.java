package com.lenis0012.bukkit.loginsecurity.hashing.lib;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

public class PHPBB3Lib {
    private String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String phpbb_hash(String password) {
        String random_state = unique_id();
        String random = "";
        int count = 6;

        if(random.length() < count) {
            random = "";

            for(int i = 0; i < count; i += 16) {
                random_state = md5(unique_id() + random_state);
                random += pack(md5(random_state));
            }
            random = random.substring(0, count);
        }

        String hash = _hash_crypt_private(
                password, _hash_gensalt_private(random, itoa64));
        if(hash.length() == 34)
            return hash;

        return md5(password);
    }

    private String unique_id() {
        return unique_id("c");
    }

    // global $config;
    // private boolean dss_seeded = false;

    private String unique_id(String extra) {
        // TODO Generate something random here.
        return "1234567890abcdef";
    }

    private String _hash_gensalt_private(String input, String itoa64) {
        return _hash_gensalt_private(input, itoa64, 6);
    }

    private String _hash_gensalt_private(String input, String itoa64, int iteration_count_log2) {
        if(iteration_count_log2 < 4 || iteration_count_log2 > 31) {
            iteration_count_log2 = 8;
        }

        String output = "$H$";
        output += itoa64.charAt(Math.min(iteration_count_log2 + 3, 30)); // PHP_VERSION >= 5 ? 5 : 3
        output += _hash_encode64(input, 6);

        return output;
    }

    /**
     * Encode hash
     */
    private String _hash_encode64(String input, int count) {
        String output = "";
        int i = 0;

        do {
            int value = input.charAt(i++);
            output += itoa64.charAt(value & 0x3f);

            if(i < count)
                value |= input.charAt(i) << 8;

            output += itoa64.charAt((value >> 6) & 0x3f);

            if(i++ >= count)
                break;

            if(i < count)
                value |= input.charAt(i) << 16;

            output += itoa64.charAt((value >> 12) & 0x3f);

            if(i++ >= count)
                break;

            output += itoa64.charAt((value >> 18) & 0x3f);
        } while(i < count);

        return output;
    }

    String _hash_crypt_private(String password, String setting) {
        String output = "*";

        // Check for correct hash
        if(!setting.substring(0, 3).equals("$H$"))
            return output;

        int count_log2 = itoa64.indexOf(setting.charAt(3));
        if(count_log2 < 7 || count_log2 > 30)
            return output;

        int count = 1 << count_log2;
        String salt = setting.substring(4, 12);
        if(salt.length() != 8)
            return output;

        String m1 = md5(salt + password);
        String hash = pack(m1);
        do {
            hash = pack(md5(hash + password));
        } while(--count > 0);

        output = setting.substring(0, 12);
        output += _hash_encode64(hash, 16);

        return output;
    }

    public boolean phpbb_check_hash(String password, String hash) {
        if(hash.length() == 34)
            return _hash_crypt_private(password, hash).equals(hash);
        else
            return md5(password).equals(hash);
    }

    public static String md5(String data) {
        try {
            byte[] bytes = data.getBytes("ISO-8859-1");
            MessageDigest md5er = MessageDigest.getInstance("MD5");
            byte[] hash = md5er.digest(bytes);
            return bytes2hex(hash);
        } catch(GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static int hexToInt(char ch) {
        if(ch >= '0' && ch <= '9')
            return ch - '0';

        ch = Character.toUpperCase(ch);
        if(ch >= 'A' && ch <= 'F')
            return ch - 'A' + 0xA;

        throw new IllegalArgumentException("Not a hex character: " + ch);
    }

    private static String bytes2hex(byte[] bytes) {
        StringBuffer r = new StringBuffer(32);
        for(int i = 0; i < bytes.length; i++) {
            String x = Integer.toHexString(bytes[i] & 0xff);
            if(x.length() < 2)
                r.append("0");
            r.append(x);
        }
        return r.toString();
    }

    static String pack(String hex) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < hex.length(); i += 2) {
            char c1 = hex.charAt(i);
            char c2 = hex.charAt(i + 1);
            char packed = (char) (hexToInt(c1) * 16 + hexToInt(c2));
            buf.append(packed);
        }
        return buf.toString();
    }
}
