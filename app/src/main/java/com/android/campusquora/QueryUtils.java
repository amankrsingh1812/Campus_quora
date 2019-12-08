package com.android.campusquora;

import android.util.Patterns;

class QueryUtils {

    public String name;

    String validateEmail(String emailInput) {
        emailInput = emailInput.trim();
        if (emailInput.isEmpty()) {
            return "Field can't be empty";
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            return "Please enter a valid email address";
        } else {
            return "";
        }
    }

    String validatePassword(String passwordInput) {
        passwordInput = passwordInput.trim();
        if (passwordInput.isEmpty()) {
            return "Field can't be empty";
        } else if (passwordInput.length() < 6) {
            return "Password too short";
        } else {
            return "";
        }
    }

}
