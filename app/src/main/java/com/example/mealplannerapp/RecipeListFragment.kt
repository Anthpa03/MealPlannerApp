package com.example.mealplannerapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentRecipeListBinding
import com.example.mealplannerapp.RecipeModel as Recipe

class RecipeListFragment : Fragment() {

    private lateinit var binding: FragmentRecipeListBinding
    private lateinit var adapter: RecipeAdapter
    private var fullRecipeList = listOf<Recipe>()
    private var filteredRecipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeListBinding.inflate(inflater, container, false)

        // Retrieve the search query and filter items
        val searchQuery = arguments?.getString("search_query") ?: ""
        binding.editTextSearch.setText(searchQuery)

        setupRecyclerView()
        setupSearchListener()

        if (searchQuery.isNotEmpty()) {
            filterRecipes(searchQuery)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        // Sample Data (Replace with actual database/fetch logic)
        fullRecipeList = listOf(
            Recipe("Cheese Pizza", "https://example.com/pizza.jpg", "15 mins"),
            Recipe("Pepperoni Pizza", "https://example.com/pizza.jpg", "20 mins"),
            Recipe("Hawaiian Pizza", "https://example.com/pizza.jpg", "25 mins"),
            Recipe("Vegan Burger", "https://example.com/burger.jpg", "10 mins"),
            Recipe("Grilled Chicken", "https://example.com/chicken.jpg", "30 mins")
        )

        //TODO: Handle recipe item click
        adapter = RecipeAdapter(filteredRecipeList) { recipe ->
        }

        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter
        filterRecipes(binding.editTextSearch.text.toString())
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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun filterRecipes(query: String) {
        filteredRecipeList.clear()

        if (query.isEmpty()) {
            filteredRecipeList.addAll(fullRecipeList)
        } else {
            filteredRecipeList.addAll(
                fullRecipeList.filter { recipe ->
                    recipe.name.contains(query, ignoreCase = true)
                }
            )
        }

        // Update view after filter
        adapter.run { notifyDataSetChanged() }
        binding.textViewResults.text = "Showing ${filteredRecipeList.size} results"
    }
}
