package com.shruti.lofo.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shruti.lofo.BindingNavigation;
import com.shruti.lofo.R;
import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.LoginRequest;
import com.shruti.lofo.data.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private Button loginButton;
    private TextView createAccountLink;
    private EditText loginEmail, loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);

        loginButton = findViewById(R.id.login_button);
        createAccountLink = findViewById(R.id.createAccountLink);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("jwt_token", null);

        // Implement this if Log out is functioning
//        if(savedToken != null){
//            startActivity(new Intent(this, BindingNavigation.class));
//            finish();
//        }

        loginButton.setOnClickListener(v -> {
            if(validateEmail() && validatePassword()) {
                loginUser();
            } else {
                Toast.makeText(Login.this, "Enter data in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        createAccountLink.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class))
        );

    } // END of onCreate

    // Validate Email
    private boolean validateEmail() {
        String val = loginEmail.getText().toString().trim();
        if (val.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
            return false;
        }
        loginEmail.setError(null);
        return true;
    }



    // Validate Password
    private boolean validatePassword() {
        String val = loginPassword.getText().toString().trim();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        loginPassword.setError(null);
        return true;
    }

    //    TODO: DONE transitioning to SQL / JWT Authentication

    private void loginUser () {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        LoginRequest request = new LoginRequest(email, password);

        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.loginUser(request);

        call.enqueue(new Callback<LoginResponse>() {

            // IF SUCCESS
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    String token = response.body().getToken();

                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("jwt_token", token);
                    editor.apply();

                    Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to Main page
                    startActivity(new Intent(Login.this, BindingNavigation.class));
                    finish();
                } else {
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_LONG).show();
                }
            }

            // SERVER ERROR (Server is offline, no internet, etc.)
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", "Network error: " + t.getMessage());
                Toast.makeText(Login.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}