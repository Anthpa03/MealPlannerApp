package com.example.mealplannerapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealplannerapp.databinding.RecyclerViewCardBinding
import com.example.mealplannerapp.RecipeModel as Recipe

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(val binding: RecyclerViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecyclerViewCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        holder.binding.textViewRecipeName.text = recipe.name
        holder.binding.textViewTtc.text = recipe.cookTime

        holder.binding.imageButtonBookmark.setOnClickListener { button ->
            button.isSelected = !button.isSelected  // Toggle selection state of bookmark button
        }

        Glide.with(holder.itemView.context)
            .load(recipe.picture)
            .placeholder(R.drawable.pizza)
            .into(holder.binding.imageViewRecipeIc)

        holder.binding.buttonViewRecipe.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size
}
