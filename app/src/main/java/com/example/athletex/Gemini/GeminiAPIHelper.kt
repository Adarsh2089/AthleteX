package com.example.athletex.Gemini

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class GeminiAPIHelper {

    private val API_KEY = "AIzaSyC_m0Ac24TcONQyu891to4FcqT0Wl2ct9s"
    private val client = OkHttpClient()
    private val URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key= $API_KEY"

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
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                println("Raw API Response: $responseData") // Debug log

                try {
                    val jsonResponse = JSONObject(responseData ?: "{}")

                    if (!jsonResponse.has("candidates")) {
                        callback("No candidates in response. Full response:\n$responseData")
                        return
                    }

                    val textResponse = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    callback(textResponse)
                } catch (e: Exception) {
                    callback("Parsing error: ${e.message}\nResponse was: $responseData")
                }
            }
        })
    }
}
