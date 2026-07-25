package ir.ac.ut.ece.ie.dto;

import ir.ac.ut.ece.ie.model.User;

/**
 * API-facing representation of a user. Unlike {@link User}, this type has no
 * passwordHash/passwordSalt fields at all, so the persistence entity can never leak
 * into an HTTP response by accident -- callers must go through {@link #from(User)}.
 */
public final class UserDto {

    private final int id;
    private final String username;
    private final String email;
    private final String phone;
    private final long createdAt;

    private UserDto(int id, String username, String email, String phone, long createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public static UserDto from(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getPhone(), user.getCreatedAt());
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
