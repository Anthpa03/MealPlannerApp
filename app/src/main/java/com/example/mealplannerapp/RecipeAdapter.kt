package com.example.mealplannerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealplannerapp.databinding.RecyclerViewCardBinding

class RecipeAdapter(
    private val recipes: MutableList<RecipeSearch.RecipeDisplayInfo>,
    private val activity: HomeActivity // Pass HomeActivity reference
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(val binding: RecyclerViewCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecyclerViewCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        val context = holder.itemView.context
        val username = SharedPreferencesManager.getUsername(context) ?: ""

        // Use the new property names from RecipeDisplayInfo
        holder.binding.textViewRecipeName.text = recipe.title
        holder.binding.textViewTtc.text = recipe.cookTime

        // Handles bookmark toggle
        val isBookmarked = SharedPreferencesManager.isBookmarked(context, username, recipe.title)
        holder.binding.imageButtonBookmark.isSelected = isBookmarked
        holder.binding.imageButtonBookmark.setOnClickListener { button ->
            if (isBookmarked) {
                SharedPreferencesManager.removeBookmark(context, username, recipe.title)
                button.isSelected = false
            } else {
                SharedPreferencesManager.addBookmark(context, username, recipe.title)
                button.isSelected = true
            }
        }

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.pizza)
            .into(holder.binding.imageViewRecipeIc)

        holder.binding.buttonViewRecipe.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("recipeId", recipe.recipeId)
                putString("title", recipe.title)
                putString("cookTime", recipe.cookTime)
                putString("imageUrl", recipe.imageUrl)
            }

            val fragment = RecipeDetailFragment()
            fragment.arguments = bundle

            activity.navigateToFragment(fragment)
        }
    }

    override fun getItemCount(): Int = recipes.size
}

