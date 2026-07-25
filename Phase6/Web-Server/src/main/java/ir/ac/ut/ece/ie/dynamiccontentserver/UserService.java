package ir.ac.ut.ece.ie.dynamiccontentserver;

import ir.ac.ut.ece.ie.dto.UserDto;
import ir.ac.ut.ece.ie.model.User;
import ir.ac.ut.ece.ie.repository.UserRepository;
import ir.ac.ut.ece.ie.security.PasswordUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getById(int userId) {
        return userRepository.findById(userId).map(UserDto::from).orElse(null);
    }

    public UpdateResult updateContact(int userId, String email, String phone) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return UpdateResult.notFound();
        }

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (!hasEmail && !hasPhone) {
            return UpdateResult.validationError("At least one of email or phone number is required");
        }
        if (hasEmail && !Validators.isValidEmail(email.trim())) {
            return UpdateResult.validationError("Email format is invalid");
        }
        if (hasPhone && !Validators.isValidPhone(phone.trim())) {
            return UpdateResult.validationError("Phone number format is invalid");
        }

        String normalizedEmail = hasEmail ? email.trim() : null;
        String normalizedPhone = hasPhone ? phone.trim() : null;

        if (normalizedEmail != null && !normalizedEmail.equals(user.getEmail())
                && userRepository.existsByEmail(normalizedEmail)) {
            return UpdateResult.conflict("Email is already registered");
        }
        if (normalizedPhone != null && !normalizedPhone.equals(user.getPhone())
                && userRepository.existsByPhone(normalizedPhone)) {
            return UpdateResult.conflict("Phone number is already registered");
        }

        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        User savedUser = userRepository.save(user);
        return UpdateResult.success(UserDto.from(savedUser));
    }

    public UpdateResult changePassword(int userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return UpdateResult.notFound();
        }

        if (currentPassword == null
                || !PasswordUtil.matches(currentPassword, user.getPasswordSalt(), user.getPasswordHash())) {
            return UpdateResult.validationError("Current password is incorrect");
        }
        if (!Validators.isValidPassword(newPassword)) {
            return UpdateResult.validationError(Validators.PASSWORD_REQUIREMENTS_MESSAGE);
        }

        String newSalt = PasswordUtil.generateSalt();
        user.setPasswordSalt(newSalt);
        user.setPasswordHash(PasswordUtil.hash(newPassword, newSalt));
        User savedUser = userRepository.save(user);
        return UpdateResult.success(UserDto.from(savedUser));
    }

    public static class UpdateResult {
        public enum Status {
            SUCCESS,
            VALIDATION_ERROR,
            CONFLICT,
            NOT_FOUND
        }

        private final Status status;
        private final String message;
        private final UserDto user;

        private UpdateResult(Status status, String message, UserDto user) {
            this.status = status;
            this.message = message;
            this.user = user;
        }

        public static UpdateResult success(UserDto user) {
            return new UpdateResult(Status.SUCCESS, null, user);
        }

        public static UpdateResult validationError(String message) {
            return new UpdateResult(Status.VALIDATION_ERROR, message, null);
        }

        public static UpdateResult conflict(String message) {
            return new UpdateResult(Status.CONFLICT, message, null);
        }

        public static UpdateResult notFound() {
            return new UpdateResult(Status.NOT_FOUND, "User not found", null);
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public UserDto getUser() {
            return user;
        }
    }
}
