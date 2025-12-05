package com.shruti.lofo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    // ⭐ 1. CRITICAL FIX: Explicit Field Declarations (Fixes "cannot find symbol: variable") ⭐
    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText; // Ang "Don't have an account? " text
    private TextView createAccountLink;  // Ang "Create Account" text

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        // ⭐ CRITICAL: Initialize the new TextView ID
        createAccountLink = findViewById(R.id.createAccountLink);

        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User already logged in and verified, go to main screen
            startActivity(new Intent(this, BindingNavigation.class));
            finish();
        }

        loginButton.setOnClickListener(v -> {
            if (!validateEmail() || !validatePassword()) { // Call to validation methods
                Toast.makeText(Login.this, "Enter data in all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser();
            }
        });


        createAccountLink.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class))
        );



    } // END of onCreate




    // ⭐ 2. CRITICAL FIX: Re-inserting the missing method definitions (Fixes "cannot find symbol: method") ⭐
    private boolean validateEmail() {
        String val = loginEmail.getText().toString().trim();
        if (val.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
            return false;
        }
        loginEmail.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String val = loginPassword.getText().toString().trim();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        loginPassword.setError(null);
        return true;
    }

    private void loginUser () {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        Log.d("LoginDebug", "Attempting login for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            // Show success message immediately after task completes
                            Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            if (user.isEmailVerified()) {
                                // 1. Login SUCCESS and VERIFIED (Navigates)
                                Log.d("LoginDebug", "Login SUCCESS: User verified. Navigating to BindingNavigation.");
                                startActivity(new Intent(Login.this, BindingNavigation.class));
                                finish();
                            } else {
                                // 2. Login SUCCESS but NOT VERIFIED (Stays on Login)
                                Log.w("LoginDebug", "Login successful, but email NOT verified.");
                                Toast.makeText(Login.this, "Please verify your email before proceeding.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // 3. Login FAILED (Stays on Login)
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown Authentication Error.";
                        Log.e("LoginDebug", "Login FAILED: " + errorMessage, task.getException());
                        Toast.makeText(Login.this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    } // END of loginUser

} // END of Login CLASSF