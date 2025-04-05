package com.example.athletex.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.athletex.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etSport, etGoals, etCoachNumber,etCoach, etExperience, etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etSport = findViewById(R.id.etSport);
        etGoals = findViewById(R.id.etGoals);
        etCoach = findViewById(R.id.etCoach);
        etExperience = findViewById(R.id.etExperience);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCoachNumber = findViewById(R.id.etCoachNumber);
        progressBar = findViewById(R.id.progressBar);
        loginBtn = findViewById(R.id.loginBtn);


        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void registerUser(View view) {
        String name = etName.getText().toString().trim();
        String sport = etSport.getText().toString().trim();
        String goals = etGoals.getText().toString().trim();
        String coach = etCoach.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Coach Number for injury managment and coach contact
        String coachNumber = etCoachNumber.getText().toString().trim();


        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sport) || TextUtils.isEmpty(goals) ||
                TextUtils.isEmpty(coach) || TextUtils.isEmpty(experience) || TextUtils.isEmpty(coachNumber) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (coachNumber.length() != 10) {
            Toast.makeText(this, "Coach Number should be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!coachNumber.matches("\\d+")) {
            Toast.makeText(this, "Coach Number should only contain digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save Coach Number for future prefernce
        SharedPreferences sharedPreferences = getSharedPreferences("CoachNumber", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("coachNumber", coachNumber);
        editor.apply();


        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = "ATX" + String.format("%04d", new Random().nextInt(10000));

                        userModel user = new userModel(userId, name, sport, goals, coach,coachNumber,experience,email);
                        mDatabase.child(mAuth.getUid()).setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
