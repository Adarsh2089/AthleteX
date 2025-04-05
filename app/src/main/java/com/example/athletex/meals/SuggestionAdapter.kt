package com.example.athletex.meals


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.athletex.Gemini.MealModel
import com.example.athletex.R

class SuggestionsAdapter(
    private val mealList: List<MealModel>,
    private val onItemClick: (MealModel) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_item, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val meal = mealList[position]
        holder.bind(meal)
        holder.itemView.setOnClickListener { onItemClick(meal) }
    }

    override fun getItemCount(): Int = mealList.size

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealName: TextView = itemView.findViewById(R.id.mealName)
       // private val mealCalories: TextView = itemView.findViewById(R.id.suggestionMealCalories)

        fun bind(meal: MealModel) {
            mealName.text = meal.name
           // mealCalories.text = "${meal.calories} cal"
        }
    }
}
