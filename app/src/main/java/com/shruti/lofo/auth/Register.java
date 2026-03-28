package com.shruti.lofo.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shruti.lofo.R;
import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.RegisterRequest;
import com.shruti.lofo.data.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private EditText signupName, signupPhone, signupEmail, signupPassword;
    private TextView loginRedirectText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPhone = findViewById(R.id.signup_phone);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginLink);

        // VALIDATION EMAIL CHECKER: HIGHLIGHT SCHOOL DOMAIN
        signupEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString();
                signupEmail.setTextColor(email.endsWith("@hcdc.edu.ph") ? Color.BLACK : Color.RED);
            }
        });

        // Handle sign-up button click
        signupButton.setOnClickListener(v -> registerUser());

        // Redirect to login screen
        loginRedirectText.setOnClickListener(v ->
                startActivity(new Intent(Register.this, Login.class)));
    }

    private void registerUser() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String phone = signupPhone.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();

        // VALIDATION CHECKS / CLAUSE
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@hcdc.edu.ph")) {
            Toast.makeText(this, "Please use your @hcdc.edu.ph  email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() != 11 || !phone.matches("\\d+")) {
            Toast.makeText(this, "Invalid phone number (must be 11 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        // IF ALL VALID, REGISTER USER
        RegisterRequest request = new RegisterRequest(email, password, name, phone);

        ApiService apiService = RetrofitClient.getApiService();
        Call<RegisterResponse> call = apiService.registerUser(request);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(Register.this, "Registered successfully!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Register.this, Login.class));
                } else {
                    Toast.makeText(Register.this, "Registration failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("RegisterError", "Network error: " + t.getMessage());
                Toast.makeText(Register.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

