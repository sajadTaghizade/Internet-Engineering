package ir.ac.ut.ece.ie.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Salted password hashing using PBKDF2WithHmacSHA256.
 *
 * PBKDF2 is chosen over a plain hash (e.g. SHA-256) because it is deliberately slow and
 * its work factor (iteration count) can be tuned upward as hardware gets faster, which is
 * exactly what NIST SP 800-132 recommends for password storage. It is also built into the
 * JDK (javax.crypto), so no extra dependency (bcrypt/argon2 library) is required. A random
 * per-user salt is generated so that two users with the same password never produce the
 * same hash, which defeats precomputed rainbow-table attacks.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plainPassword, String base64Salt) {
        byte[] salt = Base64.getDecoder().decode(base64Salt);
        PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to hash password", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean matches(String plainPassword, String base64Salt, String expectedHash) {
        String actualHash = hash(plainPassword, base64Salt);
        return constantTimeEquals(actualHash, expectedHash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
