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
    private val API_KEY = "0c2296339d27412a8d9afdf7557ee6a7"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the search query and display it in the search EditText
        val searchQuery = arguments?.getString("search_query") ?: ""
        binding.editTextSearch.setText(searchQuery)

        setupRecyclerView()
        setupSearchListener()

        // If a search query exists, fetch recipes from Spoonacular.
        // Otherwise, load a default hardcoded list.
        if (searchQuery.isNotEmpty()) {
            fetchRecipes(searchQuery)
        } else {
            loadDefaultRecipes()
        }

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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
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

    // Fetch recipes by ingredients (comma-separated) using your Spoonacular API methods
    private fun fetchRecipes(ingredientsQuery: String) {
        // Split the query string into individual ingredients
        val ingredients = ingredientsQuery.split(",").map { it.trim() }
        lifecycleScope.launch {
            // Call your search method in a background thread
            val recipeSummaries = withContext(Dispatchers.IO) {
                RecipeSearch.searchRecipesByIngredients(ingredients, apiKey = API_KEY)
            }
            // For each summary, fetch detailed info and create a RecipeDisplayInfo instance
            val recipeDisplayList = recipeSummaries?.mapNotNull { summary ->
                withContext(Dispatchers.IO) {
                    // Assume summary has an 'id' property
                    RecipeSearch.getRecipeDisplayInfo(summary.id, API_KEY)
                }
            } ?: emptyList()

            fullRecipeList = recipeDisplayList
            filterRecipes(binding.editTextSearch.text.toString())
        }
    }

    // Loads a default list of recipes in case no search query is provided
    private fun loadDefaultRecipes() {
        fullRecipeList = listOf(
            RecipeSearch.RecipeDisplayInfo(
                "Cheese Pizza",
                "https://example.com/pizza.jpg",
                "15 mins"
            ),
            RecipeSearch.RecipeDisplayInfo(
                "Pepperoni Pizza",
                "https://example.com/pizza.jpg",
                "20 mins"
            ),
            RecipeSearch.RecipeDisplayInfo(
                "Hawaiian Pizza",
                "https://example.com/pizza.jpg",
                "25 mins"
            ),
            RecipeSearch.RecipeDisplayInfo(
                "Vegan Burger",
                "https://example.com/burger.jpg",
                "10 mins"
            ),
            RecipeSearch.RecipeDisplayInfo(
                "Grilled Chicken",
                "https://example.com/chicken.jpg",
                "30 mins"
            )
        )
        filterRecipes(binding.editTextSearch.text.toString())
    }

    private fun sortRecipes(order: String) {
        when (order) {
            "ascending" -> {
            }
            "descending" -> {
            }
            else -> {
                // Default should be prioritizing bookmarks
            }
        }
    }
}
