package com.example.athletex.views.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.athletex.R
import com.example.athletex.data.database.AppRepository
import com.example.athletex.data.results.WorkoutResult
import com.example.athletex.util.MemoryManagement
import com.example.athletex.util.MyApplication
import com.example.athletex.viewmodels.ResultViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class FragmentWeeklyAnalysis : Fragment(), MemoryManagement {

    private lateinit var resultViewModel: ResultViewModel
    private var chart: BarChart? = null
    private var workoutResults: List<WorkoutResult>? = null
    private var workOutTime: TextView? = null
    private lateinit var appRepository: AppRepository
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weekly_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultViewModel = ResultViewModel(MyApplication.getInstance())
        chart = view.findViewById(R.id.chart)
        workOutTime = view.findViewById(R.id.total_time)
        appRepository = AppRepository(requireActivity().application)
        loadDataAndSetupChart()

        // To get User Name from DATABASE
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    view.findViewById<TextView>(R.id.textView1).text = name ?: "No name found"
                    Log.d("FirebaseDebug", "User Found: $name")
                } else {
                    view.findViewById<TextView>(R.id.textView1).text = "User not found!"
                    Log.d("FirebaseDebug", "User ID not found in database")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                view.findViewById<TextView>(R.id.nameTextView).text = "Error: ${error.message}"
            }
        })
    }

    private fun loadDataAndSetupChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            workoutResults = resultViewModel.getAllResult()
            val currentWeek = getCurrentCalendarWeek()

            workoutResults = workoutResults?.filter {
                getCalendarWeek(it.timestamp) == currentWeek
            }

            val decimalFormat = DecimalFormat("#.##")
            val totalWorkoutTimeForCurrentWeek =
                workoutResults?.let { calculateTotalWorkoutTimeForWeek(it, currentWeek) }
            val formattedWorkoutTime = decimalFormat.format(totalWorkoutTimeForCurrentWeek)

            withContext(Dispatchers.Main) {
                workOutTime?.text = formattedWorkoutTime

                appRepository.allPlans.observe(viewLifecycleOwner) { exercisePlans ->
                    val totalPlannedRepetitions = exercisePlans.sumOf { it.repeatCount }
                    val totalCompletedRepetitions = workoutResults?.sumOf { it.repeatedCount } ?: 0
                    val progressPercentage =
                        if (totalPlannedRepetitions != 0)
                            (totalCompletedRepetitions.toDouble() / totalPlannedRepetitions) * 100
                        else 0.0

                    updateProgressViews(progressPercentage)

                    val totalCaloriesPerDay =
                        workoutResults?.let { calculateTotalCaloriesPerDay(it) }

                    if (totalCaloriesPerDay != null) {
                        updateChart(totalCaloriesPerDay, workoutResults)
                    }
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
        workoutResults: List<WorkoutResult>,
        targetWeek: Int
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

    private fun calculateTotalCaloriesPerDay(workoutResults: List<WorkoutResult>): Map<String, Double> {
        val totalCaloriesPerDay = mutableMapOf<String, Double>()
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        daysOfWeek.forEach { totalCaloriesPerDay[it] = 0.0 }

        for (result in workoutResults) {
            val key = formatDate(getStartOfDay(result.timestamp))
            val totalCalories = totalCaloriesPerDay.getOrDefault(key, 0.0) + result.calorie
            totalCaloriesPerDay[key] = totalCalories
        }

        return totalCaloriesPerDay
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun updateChart(
        totalCaloriesPerWeek: Map<String, Double>,
        workoutResults: List<WorkoutResult>?
    ) {
        val chartView = chart ?: return // avoid null crash

        val totalCalories = workoutResults?.sumOf { it.calorie } ?: 0.0
        val formattedTotalCalories = String.format(Locale.getDefault(), "%.2f", totalCalories)
        view?.findViewById<TextView>(R.id.totalCaloriesTextView)?.text =
            getString(R.string.total_calories, formattedTotalCalories)

        val entries = totalCaloriesPerWeek.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        view?.findViewById<TextView>(R.id.total_exercise)?.text =
            getString(R.string.total_exercise, calculateTotalExerciseCountForWeek(workoutResults))

        val dataSet = BarDataSet(entries, "Total Calories per Week")
        dataSet.colors = getBarColors(totalCaloriesPerWeek)
        val data = BarData(dataSet)

        val labels = totalCaloriesPerWeek.keys.toList()
        chartView.data = data
        chartView.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartView.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartView.legend.isEnabled = false
        chartView.description = null
        chartView.axisLeft.axisMinimum = 0f

        chartView.axisRight.setDrawGridLines(false)
        chartView.axisRight.setDrawLabels(false)
        chartView.axisRight.setDrawAxisLine(false)

        chartView.axisLeft.setDrawGridLines(false)
        chartView.axisLeft.setDrawAxisLine(false)

        chartView.xAxis.setDrawGridLines(false)
        chartView.xAxis.setDrawAxisLine(false)

        if (totalCaloriesPerWeek.any { it.value == 0.0 }) {
            dataSet.valueTextSize = 0f
        }

        chartView.invalidate()
    }

    private fun calculateTotalExerciseCountForWeek(workoutResults: List<WorkoutResult>?): Int {
        return workoutResults?.count {
            val dayOfWeek = getDayOfWeek(getStartOfDay(it.timestamp))
            dayOfWeek in 1..7
        } ?: 0
    }

    private fun getDayOfWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    private fun getBarColors(totalCaloriesPerWeek: Map<String, Double>): List<Int> {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)
        return totalCaloriesPerWeek.map { (_, value) ->
            if (value > 0) primaryColor else Color.TRANSPARENT
        }
    }

    override fun clearMemory() {
        workoutResults = null
        chart?.clear()
    }
}
