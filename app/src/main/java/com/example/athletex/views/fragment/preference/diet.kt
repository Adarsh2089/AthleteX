package com.example.athletex.views.fragment.preference

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.athletex.R
import com.example.athletex.meals.MealSelection

class diet : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_diet, container, false)
        view.findViewById<View>(R.id.breakfast_add).setOnClickListener {
            launchMealSelection("Breakfast")
        }

        view.findViewById<View>(R.id.lunch_add).setOnClickListener {
            launchMealSelection("Lunch")
        }

        view.findViewById<View>(R.id.dinner_add).setOnClickListener {
            launchMealSelection("Dinner")
        }

        return view
    }
    private fun launchMealSelection(type: String) {
        val intent = Intent(activity, MealSelection::class.java)
        intent.putExtra("mealType", type)
        startActivity(intent)
    }



}