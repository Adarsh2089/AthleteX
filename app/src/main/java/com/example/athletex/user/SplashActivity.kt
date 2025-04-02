package com.example.athletex.user

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.athletex.R

/**
 * SplashActivity: A simple splash screen that appears when the app is launched.
 *
 * This activity displays a splash screen for a specified duration and then navigates
 * to the MainActivity.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)



        // Use a Handler to delay the intent navigation
            Handler(Looper.getMainLooper()).postDelayed({

                // Checks for onboarding
                prefManager = PrefManager(this)
                if (prefManager.isFirstTimeLaunch()) {
                    // Show the onboarding screen
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    // Close the splash screen
                    finish()
                }else if (isLoggedIn) {
                    // User is logged in, navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // User is not logged in, navigate to LoginActivity
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                // Start the MainActivity and finish this SplashActivity
                startActivity(intent)
                finish()
            }, 3000)// Delay for 3000 milliseconds (3 seconds)

    }
}
