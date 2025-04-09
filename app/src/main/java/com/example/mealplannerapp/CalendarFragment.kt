package com.example.mealplannerapp

import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentCalendarBinding
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {

    private lateinit var adapter: RecipeAdapter
    private var savedRecipeList = mutableListOf<RecipeSearch.RecipeDisplayInfo>()
    private var mealDatePicker: DayScrollDatePicker? = null
    private val homeViewModel: HomeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mealDatePicker = binding.mealDatePicker

        // Set up the RecyclerView first (with an empty adapter list).
        setupRecyclerView()

        // When a date is selected, load recipes for that day.
        mealDatePicker?.getSelectedDate { date ->
            if (date != null) {
                // Format the selected date to match the format in which recipes are stored.
                // For example: "Thu Apr 10 00:00:00 EDT 2025"
                val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                val targetDateString = sdf.format(date)
                Toast.makeText(requireContext(), "Loading recipes for: $targetDateString", Toast.LENGTH_SHORT).show()
                loadRecipesForDay(targetDateString)
            }
        }

        // For demonstration, update the textViewResults.
        binding.textViewResults.text = "Showing ${savedRecipeList.size} recipes"
    }

    private fun setupRecyclerView() {
        val activity = requireActivity() as HomeActivity
        adapter = RecipeAdapter(savedRecipeList, activity)
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter
    }

    /**
     * Loads the recipes saved on the specified day.
     * @param targetDateString The date string in the same format as stored, e.g., "Thu Apr 10 00:00:00 EDT 2025".
     */
    private fun loadRecipesForDay(targetDateString: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve the active username.
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No username found", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            // Load saved recipes for that day.
            val savedRecipes = homeViewModel.getSavedRecipesForDay(username, targetDateString)

            // Map each SavedRecipe to a RecipeDisplayInfo so it can be displayed using your RecipeAdapter.
            val displayList = savedRecipes.map { savedRecipe ->
                RecipeSearch.RecipeDisplayInfo(
                    title = savedRecipe.name,
                    imageUrl = savedRecipe.image,
                    cookTime = savedRecipe.cookTime,
                    recipeId = savedRecipe.recipeId
                )
            }
            // Update your adapter on the main thread.
            withContext(Dispatchers.Main) {
                savedRecipeList.clear()
                savedRecipeList.addAll(displayList)
                adapter.notifyDataSetChanged()
                binding.textViewResults.text = "Showing ${savedRecipeList.size} recipes"
            }
        }
    }
}
