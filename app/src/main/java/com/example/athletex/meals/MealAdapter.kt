package com.example.athletex.meals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.athletex.R

class MealAdapter(
    private val mealList: List<MealItem>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealCalories: TextView = itemView.findViewById(R.id.mealCalories)
        val checkBox: CheckBox = itemView.findViewById(R.id.mealCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.meal_item, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]
        holder.mealName.text = meal.name
        holder.mealCalories.text = "${meal.calories} kcal"

        // To prevent unwanted triggering
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = meal.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            meal.isSelected = isChecked
            val totalCalories = mealList.filter { it.isSelected }.sumOf { it.calories }
            onSelectionChanged(totalCalories)
        }
    }

    override fun getItemCount(): Int = mealList.size
}
