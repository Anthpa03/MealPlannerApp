package com.example.mealplannerapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.databinding.IngredientListItemBinding

class IngredientAdapter(
    private var ingredients: MutableList<RecipeIngredient>
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(val binding: IngredientListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = IngredientListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.binding.textViewIngredientName.text = ingredient.name
        holder.binding.textViewIngredientQuantity.text = "${ingredient.quantityNeeded} ${ingredient.unit}"
        // Set text color based on availability.
        val textColor = if (ingredient.isAvailable) Color.parseColor("#008000") else Color.parseColor("#FF0000")
        holder.binding.textViewIngredientName.setTextColor(textColor)
    }

    override fun getItemCount(): Int = ingredients.size

    // Update adapter data and refresh the list.
    fun updateData(newIngredients: List<RecipeIngredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }
}
// TODO:REMOVE AFTER IMPLEMENTING DB FETCHING LOGIC
data class RecipeIngredient(
    val name: String,
    val quantityNeeded: Double,
    val unit: String,
    var isAvailable: Boolean = false
)
