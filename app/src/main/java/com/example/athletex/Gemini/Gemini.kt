package com.example.athletex.Gemini

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.athletex.R

class Gemini : AppCompatActivity() {

    private lateinit var textViewResponse: TextView
    private lateinit var btnBreakfast: Button
    private lateinit var btnLunch: Button
    private lateinit var btnDinner: Button

    private val geminiHelper = GeminiAPIHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gemini)

        textViewResponse = findViewById(R.id.textViewResponse)
        btnBreakfast = findViewById(R.id.btnBreakfast)
        btnLunch = findViewById(R.id.btnLunch)
        btnDinner = findViewById(R.id.btnDinner)

        btnBreakfast.setOnClickListener {
            fetchSuggestions("breakfast")
        }

        btnLunch.setOnClickListener {
            fetchSuggestions("lunch")
        }

        btnDinner.setOnClickListener {
            fetchSuggestions("dinner")
        }
    }

    private fun fetchSuggestions(mealType: String) {
        textViewResponse.text = "Fetching $mealType suggestions..."

        geminiHelper.getMealSuggestionPrompt(mealType) { responseJson ->
            runOnUiThread {
                val meals = geminiHelper.parseMealSuggestions(responseJson)
                if (meals.isNotEmpty()) {
                    val displayText = meals.joinToString("\n\n") { meal ->
                        """
                        🍽️ ${meal.name}
                        🔸 ${meal.summary}
                        🔥 Calories: ${meal.calories}
                        ⚖️ Macros:
                            - Carbs: ${meal.macros.carbs}
                            - Protein: ${meal.macros.protein}
                            - Fat: ${meal.macros.fat}
                        """.trimIndent()
                    }
                    textViewResponse.text = displayText
                } else {
                    textViewResponse.text = "Could not parse meal suggestions."
                }
            }
        }
    }
}
