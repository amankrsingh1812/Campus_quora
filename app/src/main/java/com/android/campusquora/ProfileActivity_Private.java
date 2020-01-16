package com.android.campusquora;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class ProfileActivity_Private extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity_Private.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate Called");
        setContentView(R.layout.activity_profile__private);
    }
}
