package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple SHA-256 password hashing with salt.
 * Production systems should use BCrypt — this is beginner-friendly.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final String SEPARATOR = ":";

    /**
     * Hash a plain-text password with a random salt.
     * @return "salt:hash" encoded in Base64
     */
    public static String hash(String plainText) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashed = md.digest(plainText.getBytes());

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hashed);
            return saltB64 + SEPARATOR + hashB64;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a plain-text password against a stored hash.
     */
    public static boolean verify(String plainText, String storedHash) {
        try {
            String[] parts = storedHash.split(SEPARATOR);
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] actualHash = md.digest(plainText.getBytes());

            // Constant-time comparison to prevent timing attacks
            if (actualHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < actualHash.length; i++) {
                diff |= actualHash[i] ^ expectedHash[i];
            }
            return diff == 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * For seeding the database with known passwords during development.
     * Run this main to generate hashes for schema.sql.
     */
    public static void main(String[] args) {
        String[] passwords = {"admin123", "faculty123", "student123"};
        for (String p : passwords) {
            System.out.println(p + " => " + hash(p));
        }
    }
}
