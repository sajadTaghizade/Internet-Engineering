package ir.ac.ut.ece.ie.dynamiccontentserver;

import java.util.regex.Pattern;

final class Validators {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");

    private Validators() {
    }

    static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    static boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6
                && PASSWORD_LOWERCASE.matcher(password).find()
                && PASSWORD_UPPERCASE.matcher(password).find()
                && PASSWORD_DIGIT.matcher(password).find();
    }

    static final String PASSWORD_REQUIREMENTS_MESSAGE =
            "Password must be at least 6 characters and include lowercase, uppercase, and numeric characters";
}
