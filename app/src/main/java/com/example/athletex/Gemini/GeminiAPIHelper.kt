package com.example.athletex.Gemini

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class GeminiAPIHelper {

    private val API_KEY = "AIzaSyC_m0Ac24TcONQyu891to4FcqT0Wl2ct9s"
    private val client = OkHttpClient()
    private val URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

    fun getResponse(prompt: String, callback: (String) -> Unit) {
        val json = """
            {
                "contents": [
                    {
                        "role": "user",
                        "parts": [
                            {
                                "text": "$prompt"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiAPI", "Request failed: ${e.message}")
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("GeminiAPI", "Raw API Response: $responseData")

                try {
                    val jsonResponse = JSONObject(responseData ?: "{}")

                    if (!jsonResponse.has("candidates")) {
                        Log.e("GeminiAPI", "No candidates in response: $responseData")
                        callback("No candidates in response. Full response:\n$responseData")
                        return
                    }

                    val textResponse = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    Log.d("GeminiAPI", "Extracted text response: $textResponse")

                    callback(textResponse)
                } catch (e: Exception) {
                    Log.e("GeminiAPI", "Parsing error: ${e.message}. Response was: $responseData")
                    callback("Parsing error: ${e.message}\nResponse was: $responseData")
                }
            }
        })
    }

    // 🔥 New helper function for meal suggestions
    fun getMealSuggestionPrompt(mealType: String, callback: (String) -> Unit) {
            val prompt = """
            Generate a valid JSON response only. Do not include any explanation, markdown formatting, or extra text. Meal should be indian , vegetarian and health freak.Dish name should be known.
            Format:
            {
              \"meals\": [
                {
                  \"name\": \"Meal Name\",
                  \"calories\": \"xxx\",
                  \"summary\": \"short summary\",
                  \"macros\": {
                    \"carbs\": \"xg\",
                    \"protein\": \"xg\",
                    \"fat\": \"xg\"
                  }
                }
              ]
            }
            Generate 5 meals for $mealType.
        """.trimIndent()


        getResponse(prompt, callback)
    }

    fun parseMealSuggestions(jsonString: String): List<MealModel> {
        val mealList = mutableListOf<MealModel>()

        try {
            // Remove markdown formatting if present
            val cleanedJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedJson)
            val mealsArray = jsonObject.getJSONArray("meals")

            for (i in 0 until mealsArray.length()) {
                val meal = mealsArray.getJSONObject(i)
                val macrosObj = meal.getJSONObject("macros")

                val macros = Macros(
                    carbs = macrosObj.getString("carbs"),
                    protein = macrosObj.getString("protein"),
                    fat = macrosObj.getString("fat")
                )

                val mealModel = MealModel(
                    name = meal.getString("name"),
                    calories = meal.getString("calories"),
                    summary = meal.getString("summary"),
                    macros = macros
                )

                mealList.add(mealModel)
            }

            Log.d("GeminiAPI", "Parsed meal suggestions: $mealList")
        } catch (e: Exception) {
            Log.e("GeminiAPI", "Error parsing meal suggestions: ${e.message}")
        }

        return mealList
    }
}
