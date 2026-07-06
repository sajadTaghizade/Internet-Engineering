package ir.ac.ut.ece.ie.security;

import java.util.HashMap;

/**
 * Helper for reading the "Authorization: Bearer &lt;token&gt;" header that
 * {@code DynamicContentServer} stashes into the request params under {@code _authHeader}.
 */
public final class AuthGuard {

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthGuard() {
    }

    public static String extractBearerToken(HashMap<String, String> params) {
        String header = params.get("_authHeader");
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length()).trim();
    }

    public static JwtUtil.Claims requireAuth(HashMap<String, String> params, JwtUtil jwtUtil) {
        String token = extractBearerToken(params);
        if (token == null || token.isBlank()) {
            return null;
        }
        return jwtUtil.parseToken(token);
    }
}
