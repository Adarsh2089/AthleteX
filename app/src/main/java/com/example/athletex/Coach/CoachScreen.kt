package com.example.athletex.Coach

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.navigation.Navigation.findNavController
import com.example.athletex.R
import com.example.athletex.user.LoginActivity
import com.example.athletex.Coach.AthleteReport
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CoachScreen : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coach_screen)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Fetch user and coach info from Firebase Realtime Database
        val userId = mAuth.currentUser?.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        val weeklyReportButton = findViewById<MaterialButton>(R.id.weekly_report)
        val logOut: ImageView = findViewById(R.id.logOutBtn)


        weeklyReportButton.setOnClickListener {
            // Make the fragment container visible
            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE

            val fragment = AthleteReport()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        logOut.setOnClickListener {
            val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@CoachScreen, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        if (userId != null) {
                databaseRef.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val name = snapshot.child("name").getValue(String::class.java)
                                val coachName = snapshot.child("coach").getValue(String::class.java)

                                findViewById<TextView>(R.id.athlete_name).text =
                                    name ?: "No name found"
                                findViewById<TextView>(R.id.coach_name).text =
                                    coachName ?: "No name found"

                                Log.d("FirebaseDebug", "User Found: $name, Coach: $coachName")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Error fetching data: ${error.message}")
                        }
                    })
            }


        }
    }

