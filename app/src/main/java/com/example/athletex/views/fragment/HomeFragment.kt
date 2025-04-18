package com.example.athletex.views.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.athletex.R
import com.example.athletex.adapters.PlanAdapter
import com.example.athletex.adapters.RecentActivityAdapter
import com.example.athletex.data.database.AppRepository
import com.example.athletex.data.plan.Plan
import com.example.athletex.data.results.RecentActivityItem
import com.example.athletex.data.results.WorkoutResult
import com.example.athletex.util.MemoryManagement
import com.example.athletex.util.MyApplication
import com.example.athletex.util.MyUtils
import com.example.athletex.viewmodels.AddPlanViewModel
import com.example.athletex.viewmodels.HomeViewModel
import com.example.athletex.viewmodels.ResultViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import kotlin.math.min

class HomeFragment : Fragment(), PlanAdapter.ItemListener, MemoryManagement {
    @Suppress("PropertyName")
    val TAG = "RepDetect Home Fragment"
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private var planList: List<Plan>? = emptyList()
    private var notCompletePlanList: MutableList<Plan>? = Collections.emptyList()
    private var today: String = DateFormat.format("EEEE", Date()) as String
    private lateinit var progressText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noPlanTV: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressPercentage: TextView
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var appRepository: AppRepository
    private lateinit var addPlanViewModel: AddPlanViewModel
    private lateinit var adapter: PlanAdapter
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize RecyclerView and its adapter for recent activity
        progressText = view.findViewById(R.id.exercise_left)
        recyclerView = view.findViewById(R.id.today_plans)
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityRecyclerView)
        recentActivityAdapter = RecentActivityAdapter(emptyList())
        recentActivityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recentActivityRecyclerView.adapter = recentActivityAdapter
        noPlanTV = view.findViewById(R.id.no_plan)
        progressBar = view.findViewById(R.id.progress_bar)
        progressPercentage = view.findViewById(R.id.progress_text)
        appRepository = AppRepository(requireActivity().application)
        // Initialize ViewModel
        resultViewModel = ResultViewModel(MyApplication.getInstance())
        addPlanViewModel = AddPlanViewModel(MyApplication.getInstance())


        // To get User Name from DATABASE
       mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    view.findViewById<TextView>(R.id.nameTextView).text = name ?: "No name found"
                    Log.d("FirebaseDebug", "User Found: $name")
                } else {
                    view.findViewById<TextView>(R.id.nameTextView).text = "User not found!"
                    Log.d("FirebaseDebug", "User ID not found in database")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                view.findViewById<TextView>(R.id.nameTextView).text = "Error: ${error.message}"
            }
        })

        val addPlanButton = view.findViewById<MaterialButton>(R.id.addPlanBtn)

        addPlanButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_planStepOneFragment)
        }




        lifecycleScope.launch {
            val workoutResults = resultViewModel.getRecentWorkout()
            // Call the function to load data and set up the chart
            loadDataAndSetupChart()
            // Transform WorkoutResult objects into RecentActivityItem objects
            val imageResources = arrayOf(R.drawable.blue, R.drawable.green, R.drawable.orange)
            // Transform WorkoutResult objects into RecentActivityItem objects
            val recentActivityItems = workoutResults?.mapIndexed { index, it ->
                RecentActivityItem(
                    imageResId = imageResources[index % imageResources.size],
                    exerciseType = MyUtils.exerciseNameToDisplay(it.exerciseName),
                    reps = "${it.repeatedCount} reps"
                )
            }
            // Update the adapter with the transformed data
            recentActivityAdapter.updateData(recentActivityItems ?: emptyList())
            // Check if the recentActivityItems list is empty
            if (recentActivityItems.isNullOrEmpty()) {
                recentActivityRecyclerView.isVisible = false
                // Show a message or handle the empty case as per your UI requirements
                val noActivityMessage = view.findViewById<TextView>(R.id.no_activity_message)
                noActivityMessage.text = getString(R.string.no_activities_yet)
                noActivityMessage.isVisible = true
            } else {
                recentActivityRecyclerView.isVisible = true
            }
        }
        // Initialize home view model, RecyclerView and its adapter for today's plans
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // get the list of plans from database
        lifecycleScope.launch(Dispatchers.IO) {
            val result1 = withContext(Dispatchers.IO) { homeViewModel.getPlanByDay(today) }
            val result2 = withContext(Dispatchers.IO) { homeViewModel.getNotCompletePlans(today) }
            withContext(Dispatchers.Main) {
                updateResultFromDatabase(result1, result2)
            }
        }
    }

    private fun updateResultFromDatabase(plan: List<Plan>?, notCompleted: MutableList<Plan>?) {
        planList = plan
        notCompletePlanList = notCompleted
        adapter = PlanAdapter(requireContext())
        planList?.let {
            if (it.isNotEmpty()) {
                it.map { plan ->
                    if (plan.timeCompleted?.let { it1 -> getDayFromTimestamp(it1) } != today) {
                        lifecycleScope.launch {
                            addPlanViewModel.updateComplete(false, null, plan.id)
                        }
                    }
                }
            } else {
                Log.d(TAG, "plan list is empty")
            }
        }

//        Display the not completed plans
        val exerciseLeftString =
            resources.getString(R.string.exercise_left, notCompletePlanList?.size ?: 0)
        progressText.text = exerciseLeftString
        recyclerView.adapter = adapter
        adapter.setListener(this)
        notCompletePlanList?.let { adapter.setPlans(it) }
        updateEmptyPlan(notCompletePlanList)
    }


    private fun loadDataAndSetupChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch workout results asynchronously
            workoutResults = resultViewModel.getAllResult()
            // Filter workout results for today
            val todayWorkoutResults = workoutResults?.filter {
                isToday(it.timestamp)
            }
            // Observe exercise plans from the database
            withContext(Dispatchers.Main) {
                appRepository.allPlans.observe(viewLifecycleOwner) { exercisePlans ->
                    // Filter exercise plans for today
                    val todayExercisePlans =
                        exercisePlans?.filter { it.selectedDays.contains(today) }
                    // Calculate progress and update UI
                    val totalPlannedRepetitions = todayExercisePlans?.sumOf { it.repeatCount } ?: 0
                    val totalCompletedRepetitions =
                        todayWorkoutResults?.sumOf { it.repeatedCount } ?: 0
                    val progressPercentage = if (totalPlannedRepetitions != 0) {
                        ((totalCompletedRepetitions.toDouble() / totalPlannedRepetitions) * 100).toInt()
                    } else {
                        0
                    }
                    // Update the ProgressBar and TextView with the progress percentage
                    updateProgressViews(progressPercentage)
                }
            }
        }
    }

    // Function to update progress views (ProgressBar and TextView)
    private fun updateProgressViews(progress: Int) {
        // Check if progressPercentage is greater than 0
        if (progress > 0) {
            // Update progress views (ProgressBar and TextView)
            val cappedProgress = min(progress, 100)
            progressBar.progress = cappedProgress
            progressPercentage.text = String.format("%d%%", cappedProgress)
        } else {
            // If progressPercentage is 0 or less, hide the progress views
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
        }
    }

    // Return true if the timestamp is today's date
    private fun isToday(s: Long, locale: Locale = Locale.getDefault()): Boolean {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", locale)
            val netDate = Date(s)
            val currentDate = sdf.format(Date())
            sdf.format(netDate) == currentDate
        } catch (e: Exception) {
            false
        }
    }

    // Get the day from which the plan was marked as complete
    private fun getDayFromTimestamp(time: Long, locale: Locale = Locale.getDefault()): String? {
        return try {
            val sdf = SimpleDateFormat("EEEE", locale)
            val netDate = Date(time)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    // Delete the plan when user click on delete icon
    override fun onItemClicked(planId: Int, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        // Show a dialog for user to confirm the choice
        builder
            .setMessage("Are you sure you want to delete the plan?")
            .setTitle("Delete plan")
            .setPositiveButton("Delete") { dialog, _ ->
                // Delete the plan from database
                lifecycleScope.launch {
                    addPlanViewModel.deletePlan(planId)
                }
                notCompletePlanList?.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyPlan(notCompletePlanList)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Cancel the action
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Hide the recycler view if there are no plan left for today
    private fun updateEmptyPlan(plans: MutableList<Plan>?) {
        if (plans.isNullOrEmpty()) {
            noPlanTV.text = getString(R.string.there_is_no_plan_set_at_the_moment)
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun clearMemory() {
        planList = null
        notCompletePlanList = null
        workoutResults = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}
