package com.example.athletex.views.fragment

import android.graphics.Color
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.athletex.R
import com.example.athletex.data.database.AppRepository
import com.example.athletex.data.results.WorkoutResult
import com.example.athletex.util.MemoryManagement
import com.example.athletex.util.MyApplication
import com.example.athletex.viewmodels.ResultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min
import android.os.Bundle
import androidx.core.content.ContextCompat

/**
 * Profile view with the information on workout for a week
 */
class ProfileFragment : Fragment(), MemoryManagement {

    private lateinit var resultViewModel: ResultViewModel
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var workOutTime: TextView
    private lateinit var appRepository: AppRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel and UI components
        resultViewModel = ResultViewModel(MyApplication.getInstance())
        workOutTime = view.findViewById(R.id.total_time)
        appRepository = AppRepository(requireActivity().application)

        // Load data
        loadData()
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

    override fun clearMemory() {
        // Implementation pending
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}
