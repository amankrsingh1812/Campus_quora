package com.android.campusquora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();

        email = ((TextInputLayout) findViewById(R.id.username)).getEditText();
        email.setText(intent.getStringExtra("email"));
        password = ((TextInputLayout) findViewById(R.id.password)).getEditText();
        password.setText(intent.getStringExtra("password"));
        Button button_login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        loading = findViewById(R.id.loading);
        TextView forgotPasswordTextView = findViewById(R.id.forgotPassword);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                loginUser();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = email.getText().toString();
                sendPasswordResetEmail(emailAddress);
            }
        });
    }

    private void sendPasswordResetEmail(String emailAddress) {

        Log.v(LOG_TAG, "sendEmailPasswordRestEmail");

        if(TextUtils.isEmpty(emailAddress)) {
            Toast.makeText(getApplicationContext(), "Email Field Empty. Try Again", Toast.LENGTH_SHORT).show();
            return;
        }



        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Reset Link Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            String[] messages = task.getException().getMessage().split(":");
                            Toast.makeText(getApplicationContext(), messages[messages.length-1], Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser() {

        Log.v(LOG_TAG, "loginUser");

        String Email = email.getText().toString().trim();
        String Password = password.getText().toString().trim();

        if(TextUtils.isEmpty(Email)) {
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Email Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(Password)) {
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Password is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            loading.setVisibility(View.GONE);
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            loading.setVisibility(View.GONE);
                            String[] messages = task.getException().getMessage().split(":");
                            String message = messages[messages.length - 1];
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
