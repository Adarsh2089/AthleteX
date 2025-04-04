package com.example.athletex.Gemini

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.athletex.R
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.athletex.Gemini.GeminiAPIHelper

class Gemini : AppCompatActivity() {


    private lateinit var editTextPrompt: EditText
    private lateinit var btnGenerate: Button
    private lateinit var textViewResponse: TextView
    private val geminiHelper = GeminiAPIHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gemini)


        editTextPrompt = findViewById(R.id.editTextPrompt)
        btnGenerate = findViewById(R.id.btnGenerate)
        textViewResponse = findViewById(R.id.textViewResponse)

        btnGenerate.setOnClickListener {
            val prompt = editTextPrompt.text.toString()
            if (prompt.isNotEmpty()) {
                textViewResponse.text = "Generating response..."
                geminiHelper.getResponse(prompt) { response ->
                    runOnUiThread {
                        textViewResponse.text = response
                    }
                }
            } else {
                textViewResponse.text = "Please enter a prompt!"
            }
        }

    }
}