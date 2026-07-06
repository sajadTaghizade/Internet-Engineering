package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.model.User;
import ir.ac.ut.ece.ie.repository.UserRepository;
import ir.ac.ut.ece.ie.security.JwtUtil;
import ir.ac.ut.ece.ie.security.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResult register(String username, String password, String email, String phone) {
        String validationError = validateRegistration(username, password, email, phone);
        if (validationError != null) {
            return AuthResult.validationError(validationError);
        }

        String normalizedEmail = (email == null || email.isBlank()) ? null : email.trim();
        String normalizedPhone = (phone == null || phone.isBlank()) ? null : phone.trim();

        if (userRepository.existsByUsername(username)) {
            return AuthResult.conflict("Username is already taken");
        }
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            return AuthResult.conflict("Email is already registered");
        }
        if (normalizedPhone != null && userRepository.existsByPhone(normalizedPhone)) {
            return AuthResult.conflict("Phone number is already registered");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);
        User user = new User(username, hash, salt, normalizedEmail, normalizedPhone);
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername());
        return AuthResult.success(savedUser, token);
    }

    public AuthResult login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return AuthResult.validationError("Username and password are required");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !PasswordUtil.matches(password, user.getPasswordSalt(), user.getPasswordHash())) {
            return AuthResult.invalidCredentials();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return AuthResult.success(user, token);
    }

    private String validateRegistration(String username, String password, String email, String phone) {
        if (username == null || username.isBlank()) {
            return "Username is required";
        }
        if (!Validators.isValidPassword(password)) {
            return Validators.PASSWORD_REQUIREMENTS_MESSAGE;
        }

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            return "At least one of email or phone number is required";
        }
        if (hasEmail && !Validators.isValidEmail(email.trim())) {
            return "Email format is invalid";
        }
        if (hasPhone && !Validators.isValidPhone(phone.trim())) {
            return "Phone number format is invalid";
        }
        return null;
    }

    public static class AuthResult {
        public enum Status {
            SUCCESS,
            VALIDATION_ERROR,
            CONFLICT,
            INVALID_CREDENTIALS
        }

        private final Status status;
        private final String message;
        private final User user;
        private final String token;

        private AuthResult(Status status, String message, User user, String token) {
            this.status = status;
            this.message = message;
            this.user = user;
            this.token = token;
        }

        public static AuthResult success(User user, String token) {
            return new AuthResult(Status.SUCCESS, null, user, token);
        }

        public static AuthResult validationError(String message) {
            return new AuthResult(Status.VALIDATION_ERROR, message, null, null);
        }

        public static AuthResult conflict(String message) {
            return new AuthResult(Status.CONFLICT, message, null, null);
        }

        public static AuthResult invalidCredentials() {
            return new AuthResult(Status.INVALID_CREDENTIALS, "Invalid username or password", null, null);
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }
}
