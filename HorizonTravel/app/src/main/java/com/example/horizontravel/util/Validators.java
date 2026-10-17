package com.example.horizontravel.util;

import android.util.Patterns;

public final class Validators {
    private Validators() {}

    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= 6;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional in most registration endpoints
        }
        return phone.trim().length() >= 7 && Patterns.PHONE.matcher(phone.trim()).matches();
    }
}
