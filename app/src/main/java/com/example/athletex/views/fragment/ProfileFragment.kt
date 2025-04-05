package com.example.athletex.views.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.athletex.R
import com.example.athletex.data.database.AppRepository
import com.example.athletex.data.results.WorkoutResult
import com.example.athletex.user.LoginActivity
import com.example.athletex.util.MemoryManagement
import com.example.athletex.util.MyApplication
import com.example.athletex.viewmodels.ResultViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.jvm.java
import kotlin.math.min

class ProfileFragment : Fragment(), MemoryManagement {

    private lateinit var resultViewModel: ResultViewModel
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var workOutTime: TextView
    private lateinit var appRepository: AppRepository
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var footStepsTextView: TextView
    private lateinit var heartTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var sleepTextView: TextView
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultViewModel = ResultViewModel(MyApplication.getInstance())
        workOutTime = view.findViewById(R.id.total_time)
        appRepository = AppRepository(requireActivity().application)

        //Text Views
        footStepsTextView = view.findViewById(R.id.value_steps)
        heartTextView = view.findViewById(R.id.value_heart_rate)
        caloriesTextView = view.findViewById(R.id.value_calories)
        sleepTextView = view.findViewById(R.id.value_blood_pressure)
        val weeklySum: ImageView = view.findViewById(R.id.full_weekly_summary)
        val logOut: ImageView = view.findViewById(R.id.logOutBtn)


        // To get User Name from DATABASE
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    view.findViewById<TextView>(R.id.user_name).text = name ?: "No name found"
                    Log.d("FirebaseDebug", "User Found: $name")
                    val CoachName = snapshot.child("coach").getValue(String::class.java)
                    view.findViewById<TextView>(R.id.coach_name).text = CoachName ?: "No name found"
                    Log.d("FirebaseDebug", "User Found: $name")
                } else {
                    view.findViewById<TextView>(R.id.user_name).text = "User not found!"
                    Log.d("FirebaseDebug", "User ID not found in database")
                    view.findViewById<TextView>(R.id.coach_name).text = "User not found!"
                    Log.d("FirebaseDebug", "User ID not found in database")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                view.findViewById<TextView>(R.id.nameTextView).text = "Error: ${error.message}"
            }
        })


        // Tp Logout the User

        logOut.setOnClickListener {
            val sharedPreferences: SharedPreferences =
                requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Firebase sign out (optional but recommended)
            FirebaseAuth.getInstance().signOut()

            // Start Login Activity
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }

        loadData()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 2001)
                return
            }
        }
        // Intialize Google Fit Data Sync
            setupGoogleSignIn()

        weeklySum.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_weeklySummaryFragment)
        }
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            workoutResults = resultViewModel.getAllResult()
            val currentWeek = getCurrentCalendarWeek()
            workoutResults = workoutResults?.filter { getCalendarWeek(it.timestamp) == currentWeek }
            val decimalFormat = DecimalFormat("#.##")
            val totalWorkoutTimeForCurrentWeek =
                workoutResults?.let { calculateTotalWorkoutTimeForWeek(it, currentWeek) }
            val formattedWorkoutTime = decimalFormat.format(totalWorkoutTimeForCurrentWeek)

            withContext(Dispatchers.Main) {
                appRepository.allPlans.observe(viewLifecycleOwner) { exercisePlans ->
                    val totalPlannedRepetitions = exercisePlans.sumOf { it.repeatCount }
                    val totalCompletedRepetitions = workoutResults?.sumOf { it.repeatedCount } ?: 0
                    val progressPercentage =
                        if (totalPlannedRepetitions != 0) {
                            (totalCompletedRepetitions.toDouble() / totalPlannedRepetitions) * 100
                        } else {
                            0.0
                        }
                    workOutTime.text = formattedWorkoutTime
                    updateProgressViews(progressPercentage)
                }
            }
        }
    }

    private fun updateProgressViews(progressPercentage: Double) {
        val cappedProgress = min(progressPercentage, 110.0)
        val progressBar = view?.findViewById<ProgressBar>(R.id.progress_bar)
        val progressTextView = view?.findViewById<TextView>(R.id.percentage)
        progressBar?.progress = cappedProgress.toInt()
        progressTextView?.text = String.format("%.2f%%", cappedProgress)
    }

    private fun getCurrentCalendarWeek(): Int {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.firstDayOfWeek = Calendar.SUNDAY
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    private fun calculateTotalWorkoutTimeForWeek(
        workoutResults: List<WorkoutResult>, targetWeek: Int
    ): Double {
        return workoutResults
            .filter { getCalendarWeek(it.timestamp) == targetWeek }
            .sumOf { it.workoutTimeInMin }
    }

    private fun getCalendarWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    // ---------------------- Google Fit Integration ----------------------

    private fun setupGoogleSignIn() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .apply {
                fitnessOptions.impliedScopes.forEach { requestScopes(it) }
            }
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account == null) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)
        } else {
            fetchGoogleFitData(account)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                fetchGoogleFitData(account!!)
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed: ${e.message}")
            }
        }
    }

    private fun fetchGoogleFitData(account: GoogleSignInAccount) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(7)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_HEART_RATE_BPM)
            .read(DataType.TYPE_SLEEP_SEGMENT)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(requireContext(), account)
            .readData(readRequest)
            .addOnSuccessListener { response: DataReadResponse ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val steps = response.dataSets
                        .filter { it.dataType == DataType.TYPE_STEP_COUNT_DELTA }
                        .flatMap { it.dataPoints }
                        .sumOf { it.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt() }

                    val heartRate = response.dataSets
                        .filter { it.dataType == DataType.TYPE_HEART_RATE_BPM }
                        .flatMap { it.dataPoints }
                        .map { it.getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat() }
                        .average()

                    val sleepRate = response.dataSets
                        .filter { it.dataType == DataType.TYPE_SLEEP_SEGMENT }
                        .flatMap { it.dataPoints }
                        .map { it.getValue(DataType.TYPE_SLEEP_SEGMENT.fields[0]).asFloat() }
                        .average()

                    val calories = response.dataSets
                        .filter { it.dataType == DataType.TYPE_CALORIES_EXPENDED }
                        .flatMap { it.dataPoints }
                        .sumOf { it.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat().toDouble() }


                    withContext(Dispatchers.Main) {
                        footStepsTextView.text = steps.toString()
                        sleepTextView.text = sleepRate.toString()
                        heartTextView.text = "%.2f".format(heartRate)
                        caloriesTextView.text = "%.2f".format(calories)
                    }


                }
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "Data fetch failed: ${e.message}")
            }
    }

    override fun clearMemory() {}

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}
