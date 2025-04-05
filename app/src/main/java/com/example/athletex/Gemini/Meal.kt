package com.example.athletex.Gemini

data class MealModel(
    val name: String,
    val calories: String,
    val summary: String,
    val macros: Macros,
    var imageUrl: String = ""
)

data class Macros(
    val carbs: String,
    val protein: String,
    val fat: String
)
