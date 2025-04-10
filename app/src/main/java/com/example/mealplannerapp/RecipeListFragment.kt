package com.example.mealplannerapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentRecipeListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeListFragment : BaseFragment<FragmentRecipeListBinding>(FragmentRecipeListBinding::inflate) {

    private lateinit var adapter: RecipeAdapter  // Adapter now takes RecipeDisplayInfo items
    private var fullRecipeList = listOf<RecipeSearch.RecipeDisplayInfo>()
    private var filteredRecipeList = mutableListOf<RecipeSearch.RecipeDisplayInfo>()
    private var currentSortOrder = "default" // This tracks current sort selection
    private val API_KEY: String = BuildConfig.API_KEY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        val searchQuery = arguments?.getString("search_query") ?: ""
        val cookTimeFilter = arguments?.getString("cook_time_filter")
        val dietFilter = arguments?.getString("diet_filter")

        // Set the search query in the search EditText.
        binding.editTextSearch.setText(searchQuery)

        // Initially, hide results and show the progress bar.
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewRecipes.visibility = View.GONE
        binding.textViewResults.visibility = View.GONE
        binding.spinnerSort.visibility = View.GONE

        setupSearchListener()
        if (searchQuery.isNotEmpty()) {
            if (!cookTimeFilter.isNullOrEmpty() || !dietFilter.isNullOrEmpty()) {
                // Call your filtered search function. You'll need to parse the cookTimeFilter into min and max values.
                fetchFilteredRecipes(searchQuery, dietFilter, cookTimeFilter)
            } else {
                fetchRecipes(searchQuery)
            }
        } else {
            loadDefaultRecipes()
        }
        // Set up filter button and spinner.
        binding.imageButtonFilter.setOnClickListener {
            (activity as? HomeActivity)?.navigateToFragment(FilterListFragment())
        }

        (activity as? HomeActivity)?.setupSpinner(binding.spinnerSort) { position ->
            when (position) {
                0 -> sortRecipes("default")
                1 -> sortRecipes("ascending")
                2 -> sortRecipes("descending")
            }
        }
    }

    private fun setupRecyclerView() {
        val activity = requireActivity() as HomeActivity
        adapter = RecipeAdapter(filteredRecipeList, activity)
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter
    }


    private fun setupSearchListener() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) { }
        })
    }

    private fun filterRecipes(query: String) {
        filteredRecipeList.clear()
        if (query.isEmpty()) {
            filteredRecipeList.addAll(fullRecipeList)
        } else {
            filteredRecipeList.addAll(
                fullRecipeList.filter { recipe ->
                    recipe.title.contains(query, ignoreCase = true)
                }
            )
        }
        adapter.notifyDataSetChanged()
        binding.textViewResults.text = "Showing ${filteredRecipeList.size} results"
    }
    private fun fetchFilteredRecipes(ingredientsQuery: String, dietFilter: String?, cookTimeFilter: String?) {
        // Parse cookTimeFilter into min and max values. Example:
        var minReadyTime: Int? = null
        var maxReadyTime: Int? = null
        cookTimeFilter?.let { range ->
            when {
                range.contains("0-15") -> { minReadyTime = 0; maxReadyTime = 15 }
                range.contains("16-30") -> { minReadyTime = 16; maxReadyTime = 30 }
                range.contains("31-60") -> { minReadyTime = 31; maxReadyTime = 60 }
                range.contains(">60") -> { minReadyTime = 61; maxReadyTime = null }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val recipeSummaries = withContext(Dispatchers.IO) {
                RecipeSearch.searchRecipesByFilters(
                    ingredients = ingredientsQuery.split(",").map { it.trim() },
                    diet = dietFilter,
                    minReadyTime = minReadyTime,
                    maxReadyTime = maxReadyTime,
                    number = 15
                )
            }
            fullRecipeList = (recipeSummaries ?: emptyList()) as List<RecipeSearch.RecipeDisplayInfo>
            filterRecipes(binding.editTextSearch.text.toString())
            binding.progressBar.visibility = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
            binding.textViewResults.visibility = View.VISIBLE
            binding.spinnerSort.visibility = View.VISIBLE
        }
    }

    // Fetch recipes by ingredients (comma-separated) using your Spoonacular API methods.
    private fun fetchRecipes(ingredientsQuery: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val recipeSummaries = withContext(Dispatchers.IO) {
                // Split the query string into individual ingredients.
                val ingredients = ingredientsQuery.split(",").map { it.trim() }
                RecipeSearch.searchRecipesByIngredients(ingredients)
            }

            // Check if the view is still attached.
            if (!isAdded || view == null) return@launch

            // For each summary, fetch detailed info.
            val recipeDisplayList = recipeSummaries?.mapNotNull { summary ->
                withContext(Dispatchers.IO) {
                    RecipeSearch.getRecipeDisplayInfo(summary.id)
                }
            } ?: emptyList()

            fullRecipeList = recipeDisplayList
            filterRecipes(binding.editTextSearch.text.toString())

            // Once results are loaded, hide the progress bar and show the results views.
            binding.progressBar.visibility = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
            binding.textViewResults.visibility = View.VISIBLE
            binding.spinnerSort.visibility = View.VISIBLE
        }
    }

    // Loads a default list of recipes in case no search query is provided.
    private fun loadDefaultRecipes() {
        fullRecipeList = listOf(
            RecipeSearch.RecipeDisplayInfo(
                "Cheese Pizza",
                "https://example.com/pizza.jpg",
                "15 mins", 0
            ),
            RecipeSearch.RecipeDisplayInfo(
                "Pepperoni Pizza",
                "https://example.com/pizza.jpg",
                "20 mins", 0
            )
        )
        filterRecipes(binding.editTextSearch.text.toString())

        // Hide the progress bar and show the results.
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewRecipes.visibility = View.VISIBLE
        binding.textViewResults.visibility = View.VISIBLE
        binding.spinnerSort.visibility = View.VISIBLE
    }

    //WIP
    private fun sortRecipes(order: String) {
        val sortedList = when (order) {
            "ascending" -> {
                filteredRecipeList.sortedBy { it.title }
            }
            "descending" -> {
                filteredRecipeList.sortedByDescending { it.title }
            }
            else -> {
                // Default: Sort by bookmarked first in descending order
                filteredRecipeList.sortedByDescending { isBookmarked(it.title) }
            }
        }
        filteredRecipeList.clear()
        filteredRecipeList.addAll(sortedList)
        adapter.notifyDataSetChanged()
    }

    private fun isBookmarked(title: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("Bookmarks", 0)
        return sharedPreferences.getBoolean(title, false)
    }


}