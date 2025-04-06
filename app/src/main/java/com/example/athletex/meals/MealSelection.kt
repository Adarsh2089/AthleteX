package com.example.athletex.meals
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.athletex.Gemini.GeminiAPIHelper
import com.example.athletex.Gemini.MealModel
import com.example.athletex.R

class MealSelection : AppCompatActivity() {

    private lateinit var geminiAPIHelper: GeminiAPIHelper

    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var totalCaloriesText: TextView
    private lateinit var confirmMealButton: Button
    private lateinit var mealType: String

    private val mealList = mutableListOf<MealItem>()
    private lateinit var mealAdapter: MealAdapter

    private val suggestionsList = mutableListOf<MealModel>()
    private var totalCalories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_selection)

        mealType = intent.getStringExtra("MEAL_TYPE") ?: "Unknown"

        geminiAPIHelper = GeminiAPIHelper()

        mealRecyclerView = findViewById(R.id.mealRecyclerView)
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView)
        totalCaloriesText = findViewById(R.id.totalCalories)
        confirmMealButton = findViewById(R.id.confirmMealButton)

        setupMealRecycler()
        setupSuggestionsRecycler()

        loadMealsFromFirebase()
        loadSuggestionsData()

        confirmMealButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ref = FirebaseDatabase.getInstance().getReference("meals/$userId/$currentDate/$mealType")

            ref.removeValue().addOnSuccessListener {
                mealList.forEach { meal ->
                    val mealId = ref.push().key!!
                    ref.child(mealId).setValue(meal)
                }
                Toast.makeText(this, "Meals updated!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to update meals", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMealRecycler() {
        mealAdapter = MealAdapter(mealList) { calories ->
            totalCalories = calories
            totalCaloriesText.text = "Total Calories: $totalCalories"
        }
        mealRecyclerView.layoutManager = LinearLayoutManager(this)
        mealRecyclerView.adapter = mealAdapter
    }

    private fun setupSuggestionsRecycler() {
        suggestionsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        suggestionsRecyclerView.adapter = SuggestionsAdapter(suggestionsList) { meal ->
            showMealPopup(meal)
        }
    }

    private fun loadMealsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("meals/$userId/$currentDate/$mealType")

        databaseRef.get().addOnSuccessListener { snapshot ->
            mealList.clear()
            for (mealSnapshot in snapshot.children) {
                val meal = mealSnapshot.getValue(MealItem::class.java)
                meal?.let { mealList.add(it) }
            }
            mealAdapter.notifyDataSetChanged()
            updateTotalCalories()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch meals.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadSuggestionsData() {
        val loadingSpinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        loadingSpinner.visibility = View.VISIBLE

        geminiAPIHelper.getMealSuggestionPrompt(mealType) { jsonString ->
            val meals = geminiAPIHelper.parseMealSuggestions(jsonString)

            runOnUiThread {
                suggestionsList.clear()
                suggestionsRecyclerView.adapter?.notifyDataSetChanged()
            }

            meals.forEachIndexed { index, meal ->
                com.example.athletex.Gemini.unsplashHelper.fetchImageForMeal(meal.name) { imageUrl ->
                    meal.imageUrl = imageUrl ?: ""
                    runOnUiThread {
                        suggestionsList.add(meal)
                        suggestionsRecyclerView.adapter?.notifyItemInserted(suggestionsList.size - 1)

                        if (index == meals.lastIndex) {
                            loadingSpinner.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showMealPopup(meal: MealModel) {
        val dialogView = layoutInflater.inflate(R.layout.meal_popup, null)

        val description = dialogView.findViewById<TextView>(R.id.mealDescription)
        val nutrientInfo = dialogView.findViewById<TextView>(R.id.nutrientInfo)
        val mealImage = dialogView.findViewById<ImageView>(R.id.mealImage)

        description.text = meal.summary
        nutrientInfo.text = "🥩 Protein: ${meal.macros.protein}g  |  🍞 Carbs: ${meal.macros.carbs}g  |  🥑 Fats: ${meal.macros.fat}g"

        if (meal.imageUrl.isNotEmpty()) {
            Glide.with(this).load(meal.imageUrl).into(mealImage)
        } else {
            mealImage.setImageResource(R.drawable.agnifit)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add to Meal") { _, _ ->
                val newMeal = MealItem(meal.name, meal.calories.toInt(), true)
                mealList.add(newMeal)
                mealAdapter.notifyItemInserted(mealList.size - 1)
                updateTotalCalories()
                saveMealToFirebase(newMeal)
            }

            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun saveMealToFirebase(meal: MealItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("meals/$userId/$currentDate/$mealType")

        val mealId = databaseRef.push().key!!
        databaseRef.child(mealId).setValue(meal)
    }

    private fun updateTotalCalories() {
        val selectedCalories = mealList.filter { it.isSelected }.sumOf { it.calories }
        totalCalories = selectedCalories
        totalCaloriesText.text = "Total Calories: $totalCalories"
    }





}
