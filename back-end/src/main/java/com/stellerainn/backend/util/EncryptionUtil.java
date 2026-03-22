package com.stellerainn.backend.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    // 16 byte static key for AES (In production, this should be an environment variable)
    private static final String SECRET_KEY = "ACIR_SECRET_KEYS"; 

    public static String encrypt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            System.err.println("Encryption error: " + ex.getMessage());
            return value;
        }
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return encrypted;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original, "UTF-8");
        } catch (Exception ex) {
            // Might not be encrypted, return original
            return encrypted;
        }
    }
}
