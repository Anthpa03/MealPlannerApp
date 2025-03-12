package com.example.mealplannerapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealplannerapp.databinding.RecyclerViewCardBinding

class RecipeAdapter(
    private val recipes: MutableList<RecipeSearch.RecipeDisplayInfo>,
    private val onItemClick: (RecipeSearch.RecipeDisplayInfo) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(val binding: RecyclerViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecyclerViewCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        // Use the new property names from RecipeDisplayInfo
        holder.binding.textViewRecipeName.text = recipe.title
        holder.binding.textViewTtc.text = recipe.cookTime

        holder.binding.imageButtonBookmark.setOnClickListener { button ->
            button.isSelected = !button.isSelected  // Toggle bookmark selection
        }

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.pizza)
            .into(holder.binding.imageViewRecipeIc)

        holder.binding.buttonViewRecipe.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size
}

