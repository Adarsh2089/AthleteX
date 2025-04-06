package com.example.athletex.meals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.athletex.Gemini.MealModel
import com.example.athletex.R

class SuggestionsAdapter(
    private val mealList: List<MealModel>,
    private val onItemClick: (MealModel) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    // ✅ This is the ONLY ViewHolder you need
    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_item, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val meal = mealList[position]
        holder.mealName.text = meal.name

        if (meal.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(meal.imageUrl)
                .placeholder(R.drawable.agnifit)
                .into(holder.mealImage)
        } else {
            holder.mealImage.setImageResource(R.drawable.agnifit)
        }

        holder.itemView.setOnClickListener {
            onItemClick(meal)
        }
    }

    override fun getItemCount(): Int = mealList.size
}
