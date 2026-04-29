package com.education.platform.agora;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class TokenUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenUtils() {
    }

    static int nowSeconds() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    static int randomInt() {
        return RANDOM.nextInt();
    }

    static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    static byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
    }

    static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        byte[] buffer = new byte[Math.max(1024, data.length * 2)];
        int compressed = deflater.deflate(buffer);
        deflater.end();
        byte[] out = new byte[compressed];
        System.arraycopy(buffer, 0, out, 0, compressed);
        return out;
    }

    static byte[] decompress(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        byte[] buffer = new byte[8192];
        try {
            int len = inflater.inflate(buffer);
            byte[] out = new byte[len];
            System.arraycopy(buffer, 0, out, 0, len);
            return out;
        } catch (Exception e) {
            return new byte[0];
        } finally {
            inflater.end();
        }
    }

    static boolean isAgoraUuid(String value) {
        return value != null && value.matches("^[A-Fa-f0-9]{32}$");
    }
}
