package com.example.mealplannerapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve and display the username.
        val username = SharedPreferencesManager.getUsername(requireContext())
        binding.textViewUsername.text = username

        // Set up main UI listeners as before.
        binding.editTextSearch.setOnClickListener { navigateToRecipeList() }
        binding.imageButtonSearch.setOnClickListener { navigateToRecipeList() }
        binding.imageButtonFilter.setOnClickListener {
            (activity as? HomeActivity)?.navigateToFragment(FilterListFragment())
        }

        // Load the recently saved recipes into the new RecyclerView.
        loadRecentlySavedRecipes()
    }

    /**
     * Loads the three most recently saved recipes for the current user and displays them in the home fragment.
     * The SavedRecipe objects are mapped to RecipeDisplayInfo objects to be used by the RecipeAdapter.
     */
    private fun loadRecentlySavedRecipes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                // Optionally, handle the error.
                return@launch
            }
            // Retrieve the managed user from Realm.

            // Get the recently saved recipes list, or an empty list if none found.
            val recentRecipes = homeViewModel.getRecentlySavedRecipesForUser(username)
            // Map each SavedRecipe to a RecipeDisplayInfo.
            val displayList = recentRecipes.map { savedRecipe ->
                RecipeSearch.RecipeDisplayInfo(
                    title = savedRecipe.name,
                    imageUrl = savedRecipe.image,
                    cookTime = savedRecipe.cookTime,
                    recipeId = savedRecipe.recipeId
                )
            }
            withContext(Dispatchers.Main) {
                // Setup the recycler view.
                // Using horizontal layout so the three recipes can be easily viewed.
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                // Initialize the adapter with the mapped list.
                binding.recyclerView.adapter =
                    RecipeAdapter(displayList.toMutableList(), (activity as HomeActivity))
            }
        }
    }

    private fun navigateToRecipeList() {
        val searchQuery = binding.editTextSearch.text.toString().trim()
        val fragment = RecipeListFragment().apply {
            arguments = Bundle().apply {
                putString("search_query", searchQuery)
            }
        }
        (activity as? HomeActivity)?.navigateToFragment(fragment)
    }
}
