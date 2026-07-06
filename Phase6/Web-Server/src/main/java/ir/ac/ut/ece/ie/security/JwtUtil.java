package ir.ac.ut.ece.ie.security;

import ir.ac.ut.ece.ie.dynamiccontentserver.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal, dependency-free JSON Web Token implementation (HS256).
 *
 * The token is signed with HMAC-SHA256 (a symmetric algorithm) rather than an asymmetric
 * one (e.g. RS256) because a single backend service both issues and verifies the tokens
 * here; there is no third party that needs to verify tokens with a public key, so a shared
 * secret is simpler to operate and equally secure for this use case. The implementation is
 * written by hand (instead of pulling in a library such as jjwt) so the header/payload/
 * signature structure required by the assignment is fully transparent.
 *
 * Structure: base64url(header) + "." + base64url(payload) + "." + base64url(HMAC-SHA256 signature)
 */
@Component
public class JwtUtil {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*(\\d+)");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("\"username\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final byte[] secretBytes;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:1440}") long expirationMinutes) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    public String generateToken(int userId, String username) {
        long now = System.currentTimeMillis();
        String payload = "{\"sub\":" + userId
                + ",\"username\":" + JsonUtils.quote(username)
                + ",\"iat\":" + now
                + ",\"exp\":" + (now + expirationMillis)
                + "}";

        String headerPart = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadPart = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerPart + "." + payloadPart;
        String signaturePart = base64UrlEncode(sign(signingInput));

        return signingInput + "." + signaturePart;
    }

    public Claims parseToken(String token) {
        if (token == null) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSignature = sign(signingInput);
        byte[] providedSignature;
        try {
            providedSignature = Base64.getUrlDecoder().decode(parts[2]);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (!constantTimeEquals(expectedSignature, providedSignature)) {
            return null;
        }

        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Matcher subMatcher = SUB_PATTERN.matcher(payload);
        Matcher expMatcher = EXP_PATTERN.matcher(payload);
        Matcher usernameMatcher = USERNAME_PATTERN.matcher(payload);

        if (!subMatcher.find() || !expMatcher.find() || !usernameMatcher.find()) {
            return null;
        }

        long exp = Long.parseLong(expMatcher.group(1));
        if (exp < System.currentTimeMillis()) {
            return null;
        }

        int userId = Integer.parseInt(subMatcher.group(1));
        String username = usernameMatcher.group(1);
        return new Claims(userId, username);
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    public static final class Claims {
        private final int userId;
        private final String username;

        public Claims(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
