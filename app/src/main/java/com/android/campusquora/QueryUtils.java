package com.android.campusquora;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Random;


class QueryUtils {

    public String name;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    String validateEmail(String emailInput) {
        Log.v(LOG_TAG, "validateEmail Called");
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
        Log.v(LOG_TAG, "validatePassword Called");
        passwordInput = passwordInput.trim();
        if (passwordInput.isEmpty()) {
            return "Field can't be empty";
        } else if (passwordInput.length() < 6) {
            return "Password too short";
        } else {
            return "";
        }
    }

    String generateUniqueId() {
        Log.v(LOG_TAG, "generateUniqueId Called");
        Random random = new Random();
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder unique_id = new StringBuilder();
        for(int i = 0; i < 20; ++i) {
            unique_id.append(chars.charAt(random.nextInt(chars.length())));
        }
        return unique_id.toString();
    }

}
