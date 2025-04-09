package com.example.mealplannerapp


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mealplannerapp.databinding.FragmentRecipeDetailsBinding
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailsBinding>(FragmentRecipeDetailsBinding::inflate) {

    private lateinit var ingredientAdapter: IngredientAdapter
    // Hold the current recipe details for later use.
    private var currentRecipe: RecipeSearch.RecipeDisplayInfo? = null

    // Inject HomeViewModel to retrieve user's owned ingredients.
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the recipeId passed from RecipeAdapter.
        val recipeId = arguments?.getInt("recipeId") ?: 0
        if (recipeId == 0) {
            Log.e("RecipeDetailFragment", "Invalid recipeId provided.")
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        // Initially, show the progress bar and hide the recipe details.
        binding.progressBar2.visibility = View.VISIBLE
        binding.imageViewRecipe.visibility = View.INVISIBLE
        binding.textViewRecipeTitle.visibility = View.INVISIBLE
        binding.textViewCookTime.visibility = View.INVISIBLE
        binding.textViewInstructions.visibility = View.INVISIBLE
        binding.recyclerViewIngredients.visibility = View.INVISIBLE
        binding.buttonIngredients.visibility = View.INVISIBLE
        binding.buttonInstructions.visibility = View.INVISIBLE
        binding.imageButtonAdd.visibility = View.INVISIBLE

        // Enable scrolling on the instructions text view.
        binding.textViewInstructions.movementMethod = ScrollingMovementMethod()

        // Initialize the RecyclerView adapter with an empty list.
        ingredientAdapter = IngredientAdapter(mutableListOf())
        binding.recyclerViewIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIngredients.adapter = ingredientAdapter

        // Fetch the recipe details asynchronously using only the recipeId.
        lifecycleScope.launch {
            val details = withContext(Dispatchers.IO) {
                RecipeSearch.getRecipeDetails(recipeId)
            }
            if (details != null) {
                // Update UI elements with the fetched details.
                binding.textViewRecipeTitle.text = details.title
                binding.textViewCookTime.text = "Cook Time: ${details.readyInMinutes} mins"
                Glide.with(this@RecipeDetailFragment)
                    .load(details.image)
                    .placeholder(R.drawable.pizza)
                    .into(binding.imageViewRecipe)

                // Update the instructions, using a placeholder if needed.
                binding.textViewInstructions.text = if (!details.instructions.isNullOrEmpty()) {
                    details.instructions
                } else {
                    getString(R.string.placeholder_string)
                }

                // Retrieve the user's owned ingredients.
                val username = SharedPreferencesManager.getUsername(requireContext())
                val userInventory = if (!username.isNullOrEmpty()) {
                    homeViewModel.getIngredientsForUser(username)
                } else {
                    emptyList()
                }
                val ownedNames = userInventory.map { it.name }

                // Map the API's extendedIngredients to RecipeIngredient objects,
                // marking each as available if a matching owned ingredient is found.
                val newIngredients = details.extendedIngredients.map { apiIngredient ->
                    RecipeIngredient(
                        name = apiIngredient.name,
                        quantityNeeded = apiIngredient.amount,
                        unit = apiIngredient.unit,
                        isAvailable = ownedNames.any { normalize(it) == normalize(apiIngredient.name) }
                    )
                }
                // Update the RecyclerView adapter with the new ingredients.
                ingredientAdapter.updateData(newIngredients)

                // Create a RecipeDisplayInfo object from the fetched details.
                currentRecipe = RecipeSearch.RecipeDisplayInfo(
                    title = details.title,
                    cookTime = "${details.readyInMinutes} mins",
                    imageUrl = details.image,
                    recipeId = details.id
                )

                // Now that data is loaded, hide the progress bar and show the details.
                binding.progressBar2.visibility = View.GONE
                binding.imageViewRecipe.visibility = View.VISIBLE
                binding.textViewRecipeTitle.visibility = View.VISIBLE
                binding.textViewCookTime.visibility = View.VISIBLE
                // Show only ingredients by default.
                binding.recyclerViewIngredients.visibility = View.VISIBLE
                binding.textViewInstructions.visibility = View.GONE
                binding.buttonIngredients.visibility = View.VISIBLE
                binding.buttonInstructions.visibility = View.VISIBLE
                binding.imageButtonAdd.visibility = View.VISIBLE
            } else {
                binding.progressBar2.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to load recipe details", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the back button to return to the previous screen.
        binding.imageButtonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Toggle between viewing ingredients and instructions.
        binding.buttonIngredients.setOnClickListener {
            binding.recyclerViewIngredients.visibility = View.VISIBLE
            binding.textViewInstructions.visibility = View.GONE
        }
        binding.buttonInstructions.setOnClickListener {
            binding.recyclerViewIngredients.visibility = View.GONE
            binding.textViewInstructions.visibility = View.VISIBLE
        }

        // Show the bottom dialog when the add button is clicked.
        binding.imageButtonAdd.setOnClickListener {
            currentRecipe?.let {
                showBottomDialog(it)
            } ?: Toast.makeText(requireContext(), "Recipe details not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to "normalize" an ingredient name for comparison.
    private fun normalize(name: String): String {
        return name.trim().lowercase(Locale.getDefault()).removeSuffix("s")
    }

    private fun showBottomDialog(recipe: RecipeSearch.RecipeDisplayInfo) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_dialog_add_recipe)

        val datePicker = dialog.findViewById<DayScrollDatePicker>(R.id.meal_add_date_picker)
        val buttonAddRecipe = dialog.findViewById<Button>(R.id.button_add_recipe)
        val cancelButton = dialog.findViewById<ImageButton>(R.id.cancelButton)

        var selectedDate: String? = null

        // Retrieve the selected date from the DatePicker.
        datePicker?.getSelectedDate { date ->
            selectedDate = date.toString()
        }

        buttonAddRecipe.setOnClickListener {
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Save the recipe with the chosen date.
            saveRecipeForDate(recipe, selectedDate!!)
            Toast.makeText(requireContext(), "Recipe saved for $selectedDate", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun saveRecipeForDate(recipe: RecipeSearch.RecipeDisplayInfo, selectedDate: String) {
        // Retrieve the active username.
        val username = SharedPreferencesManager.getUsername(requireContext())
        if (username.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No username found", Toast.LENGTH_SHORT).show()
            return
        }
        // For this example, we'll pass an empty list for ingredients,
        // since RecipeDisplayInfo doesn't include detailed ingredients.
        // You can modify this if you have a detailed recipe to supply.
        homeViewModel.saveRecipeForUser(
            username = username,
            recipeId = recipe.recipeId,
            name = recipe.title,
            ingredients = emptyList(), // or pass the actual list if available
            cookTime = recipe.cookTime,
            instructions = "",         // You can pass instructions if available
            image = recipe.imageUrl,
            date = selectedDate
        )
        Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
    }

}
