package com.example.athletex.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.athletex.Activities.CoachorUser;
import com.example.athletex.Gemini.Gemini;
import com.example.athletex.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private TextView forgotPasswordText,signupBTN;
    private RadioGroup userTypeGroup;
    private RadioButton radioAthlete, radioCoach;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signupBTN = findViewById(R.id.signup);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        radioAthlete = findViewById(R.id.radioAthlete);
        radioCoach = findViewById(R.id.radioCoach);


        mAuth = FirebaseAuth.getInstance();

        forgotPasswordText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        signupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CoachorUser.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void loginUser(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        saveLoginSession();
                        // Check Whether the user is a Coach or an Athlete
                        int selectedId = userTypeGroup.getCheckedRadioButtonId();
                        if (selectedId == R.id.radioAthlete) {
                            // User is an Athlete
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // User is a Coach
                            startActivity(new Intent(LoginActivity.this, Gemini.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(LoginActivity.this, "Please check your password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLoginSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }
}
