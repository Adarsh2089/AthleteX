package com.example.athletex.meals

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.athletex.Gemini.GeminiAPIHelper
import com.example.athletex.R
import com.example.athletex.Gemini.MealModel
import com.example.athletex.Gemini.Macros

class MealSelection : AppCompatActivity() {

    private lateinit var geminiAPIHelper: GeminiAPIHelper


    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var totalCaloriesText: TextView
    private lateinit var mealType: String

    private val suggestionsList = mutableListOf<MealModel>()
    private var totalCalories = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_selection)

        // Get the meal type passed from previous screen
        mealType = intent.getStringExtra("MEAL_TYPE") ?: "Unknown"

        // Initialize views
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView)
        totalCaloriesText = findViewById(R.id.totalCalories)

        geminiAPIHelper = GeminiAPIHelper()


        // Setup recyclerview
        setupSuggestionsRecycler()

        // Load dummy suggestion data
        loadSuggestionsData()
    }

    private fun setupSuggestionsRecycler() {
        suggestionsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        suggestionsRecyclerView.adapter = SuggestionsAdapter(suggestionsList) { meal ->
            showMealPopup(meal)
        }
    }

    private fun loadSuggestionsData() {
        val loadingSpinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        loadingSpinner.visibility = View.VISIBLE

        geminiAPIHelper.getMealSuggestionPrompt(mealType) { jsonString ->
            val meals = geminiAPIHelper.parseMealSuggestions(jsonString)

            // Clear old data
            runOnUiThread {
                suggestionsList.clear()
                suggestionsRecyclerView.adapter?.notifyDataSetChanged()
            }

            // Fetch image for each meal
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


//    private fun showMealPopup(meal: MealModel) {
//        val message = """
//            Calories: ${meal.calories}
//            Summary: ${meal.summary}
//            Macros:
//            • Carbs: ${meal.macros.carbs}
//            • Protein: ${meal.macros.protein}
//            • Fat: ${meal.macros.fat}
//        """.trimIndent()
//
//        AlertDialog.Builder(this)
//            .setTitle(meal.name)
//            .setMessage(message)
//            .setPositiveButton("Add") { _, _ ->
//                Toast.makeText(this, "${meal.name} added to meal", Toast.LENGTH_SHORT).show()
//                totalCalories += meal.calories.toInt()
//                totalCaloriesText.text = "Total Calories: $totalCalories"
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

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
            mealImage.setImageResource(R.drawable.agnifit) // fallback
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add to Meal") { _, _ ->
                Toast.makeText(this, "${meal.name} added!", Toast.LENGTH_SHORT).show()
                totalCalories += meal.calories.toInt()
                totalCaloriesText.text = "Total Calories: $totalCalories"
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


}
