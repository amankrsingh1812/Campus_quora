package com.android.campusquora;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 8447;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextInputLayout email;
    private TextInputLayout password;
    private Button button_register;
    private Button button_login;
    private QueryUtils utils = new QueryUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.v(LOG_TAG, "onCreate");

        email = findViewById(R.id.signup_email_input);
        password = findViewById(R.id.signup_password_input);
        button_register = findViewById(R.id.button_register);
        button_login = findViewById(R.id.button_login);
        ImageView login_google = findViewById(R.id.login_google);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        email.getEditText().addTextChangedListener(new RegistrationTextWatcher(email));
        password.getEditText().addTextChangedListener(new RegistrationTextWatcher(password));

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                loginIntent.putExtra("email", email.getEditText().getText().toString());
                loginIntent.putExtra("password", password.getEditText().getText().toString());
                startActivity(loginIntent);
            }
        });

        login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this, "Google", Toast.LENGTH_SHORT).show();
                Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.v(LOG_TAG, "onActivityResult");

        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult();
                if (googleSignInAccount != null) {
                    firebaseAuthWithGoogle(googleSignInAccount);
                } else {
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.v(LOG_TAG, "firebaseAuthWithGoogle");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Could not register. Try Again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void registerUser(View view) {

        Log.v(LOG_TAG, "registerUser");
        String Email = email.getEditText().getText().toString().trim();
        String Password = password.getEditText().getText().toString().trim();

        if(!utils.validateEmail(Email).isEmpty() || !utils.validatePassword(Password).isEmpty()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(RegisterActivity.this, "Could not register. Try Again." + task.getResult().toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (RuntimeExecutionException e) {
                            String[] messages = e.toString().split(":");
                            String message = messages[messages.length-1];
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private class RegistrationTextWatcher implements TextWatcher {

        private TextInputLayout inputLayout;
        RegistrationTextWatcher(TextInputLayout inputLayout) {
            this.inputLayout = inputLayout;
        }

        private void setError(TextInputLayout inputLayout, String error) {
            if(!error.equals("")) {
                inputLayout.setError(error);
                button_register.setEnabled(false);
            } else {
                inputLayout.setErrorEnabled(false);
                button_register.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String error;
            int id = inputLayout.getId();
            if(id == R.id.signup_email_input) {
                error = utils.validateEmail(editable.toString());
            } else if(id == R.id.signup_password_input) {
                error = utils.validatePassword(editable.toString());
            } else {
                error = "";
            }

            setError(inputLayout, error);
        }
    }
}