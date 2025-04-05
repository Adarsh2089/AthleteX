package com.example.athletex.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.athletex.R;
import com.example.athletex.user.LoginActivity;
import com.example.athletex.user.SignupActivity;
import com.example.athletex.Coach.CoachSignup;
import com.google.android.material.button.MaterialButton;

public class CoachorUser extends AppCompatActivity {

    private MaterialButton CoachSignUpBtn;
    private MaterialButton AthleteSignupBtn;
    private TextView CoachloginBtn;
    private TextView AthleteloginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coachor_user);

        CoachSignUpBtn = findViewById(R.id.CoachSignUpBtn);
        AthleteSignupBtn = findViewById(R.id.AthleteSignupBtn);
        CoachloginBtn = findViewById(R.id.CoachloginBtn);
        AthleteloginBtn = findViewById(R.id.AthleteloginBtn);

        CoachSignUpBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CoachSignup.class));
        });

        AthleteSignupBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

       CoachloginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });


        AthleteloginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

    }
}