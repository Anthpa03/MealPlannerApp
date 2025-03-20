package com.example.mealplannerapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mealplannerapp.databinding.FragmentRecipeDetailsBinding

class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailsBinding>(FragmentRecipeDetailsBinding::inflate) {
    private lateinit var ingredientAdapter: IngredientAdapter
    private var ingredientList: List<RecipeIngredient> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO:replace example ingredients with real ingredients
        val recipeIngredients = listOf(
            RecipeIngredient("Flour", 250.0, "g"),
            RecipeIngredient("Cheese", 200.0, "g"),
            RecipeIngredient("Basil", .5, "tsp"),
            RecipeIngredient("Tomato Sauce", 100.0, "ml"),
            RecipeIngredient("Salt", 1.0, "tsp"),
            RecipeIngredient("Oregano", .5, "tsp"),
            RecipeIngredient("Yeast", 10.0, "g"),
            RecipeIngredient("Olive Oil", 1.0, "tbsp")
        )

        // Get user's ingredients and check availability
        ingredientList = checkIngredientAvailability(recipeIngredients)

        // Retrieve arguments passed from RecipeAdapter
        val title = arguments?.getString("title")
        val cookTime = arguments?.getString("cookTime")
        val imageUrl = arguments?.getString("imageUrl")

        // Set data in the UI
        binding.textViewRecipeTitle.text = title
        binding.textViewCookTime.text = "Cook Time: $cookTime"
        binding.textViewInstructions.movementMethod = ScrollingMovementMethod()

        // Load recipe image
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.pizza)
            .into(binding.imageViewRecipe)

        setupRecyclerView()

        // Set Ingredients as the default selected option
        binding.recyclerViewIngredients.visibility = View.VISIBLE
        binding.textViewInstructions.visibility = View.GONE

        binding.imageButtonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.buttonIngredients.setOnClickListener {
            binding.recyclerViewIngredients.visibility = View.VISIBLE
            binding.textViewInstructions.visibility = View.GONE
        }

        binding.buttonInstructions.setOnClickListener {
            binding.recyclerViewIngredients.visibility = View.GONE
            binding.textViewInstructions.visibility = View.VISIBLE
        }

    }

    private fun getUserIngredients(): Map<String, Double> {
        //TODO: Replace with database fetching
        return mapOf(
            "Flour" to 500.0,
            "Cheese" to 200.0,
            "Tomato Sauce" to 100.0
        )
    }

    private fun checkIngredientAvailability(recipeIngredients: List<RecipeIngredient>): List<RecipeIngredient> {
        val userIngredients = getUserIngredients()
        return recipeIngredients.map { ingredient ->
            val availableQuantity = userIngredients[ingredient.name] ?: 0.0
            ingredient.isAvailable = availableQuantity >= ingredient.quantityNeeded
            ingredient
        }
    }

    private fun setupRecyclerView() {
        ingredientAdapter = IngredientAdapter(ingredientList)
        binding.recyclerViewIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIngredients.adapter = ingredientAdapter
    }
}
