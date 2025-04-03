package com.example.mealplannerapp

import InventoryItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentFiltersBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@AndroidEntryPoint
class FilterListFragment : BaseFragment<FragmentFiltersBinding>(FragmentFiltersBinding::inflate) {

    private lateinit var adapter: FilterIngredientAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private var userIngredients: List<InventoryItem> = listOf()
    private var selectedCookTimeRange: String? = null
    private val selectedDiets = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and load the user's ingredients.
        val view = super.onCreateView(inflater, container, savedInstanceState)
        loadUserIngredients()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exit button functionality.
        binding.imageButtonExit.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set up cook time spinner with the desired ranges.
        val cookTimeRanges = listOf("0-15 minutes", "16-30 minutes", "31-60 minutes", ">60 minutes")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cookTimeRanges)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ttcSpinner.adapter = spinnerAdapter
        binding.ttcSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedCookTimeRange = cookTimeRanges[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCookTimeRange = null
            }
        }

        // Listen for diet filter changes via the ChipGroup.
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedDiets.clear()
            checkedIds.forEach { id ->
                when (id) {
                    R.id.chip_vegetarian -> selectedDiets.add("Vegetarian")
                    R.id.chip_vegan -> selectedDiets.add("Vegan")
                    R.id.chip_gluten -> selectedDiets.add("Gluten-Free")
                }
            }
        }

        binding.buttonSearchByIngredients.setOnClickListener {
            // Get selected ingredients.
            val selectedIngredients = adapter.getSelectedIngredients().map { it.name }
            if (selectedIngredients.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one ingredient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // For diet, if multiple are selected, we simply use the first; if none selected, use null.
            val diet = if (selectedDiets.isNotEmpty()) selectedDiets[0] else null

            // Parse the cook time spinner range.
            var minReadyTime: Int? = null
            var maxReadyTime: Int? = null
            selectedCookTimeRange?.let { range ->
                when {
                    range.contains("0-15") -> { minReadyTime = 0; maxReadyTime = 15 }
                    range.contains("16-30") -> { minReadyTime = 16; maxReadyTime = 30 }
                    range.contains("31-60") -> { minReadyTime = 31; maxReadyTime = 60 }
                    range.contains(">60")   -> { minReadyTime = 61; maxReadyTime = null }
                }
            }

            lifecycleScope.launch {
                // First, try to search with the provided time constraints.
                val filteredRecipes = withContext(Dispatchers.IO) {
                    RecipeSearch.searchRecipesByFilters(
                        ingredients = selectedIngredients,
                        diet = diet,
                        minReadyTime = minReadyTime,
                        maxReadyTime = maxReadyTime,
                        number = 15
                    )
                }

                if (filteredRecipes != null && filteredRecipes.isNotEmpty()) {
                    // Navigate to RecipeListFragment with the filters.
                    val fragment = RecipeListFragment().apply {
                        arguments = Bundle().apply {
                            putString("search_query", selectedIngredients.joinToString(","))
                            putString("cook_time_filter", selectedCookTimeRange)
                            putString("diet_filter", diet ?: "")
                        }
                    }
                    (activity as? HomeActivity)?.navigateToFragment(fragment)
                } else {
                    // No results found with time constraints.
                    Toast.makeText(requireContext(), "No recipes found for selected time. Expanding search...", Toast.LENGTH_SHORT).show()

                    // Re-run the search without time constraints.
                    val expandedRecipes = withContext(Dispatchers.IO) {
                        RecipeSearch.searchRecipesByFilters(
                            ingredients = selectedIngredients,
                            diet = diet,
                            minReadyTime = null,
                            maxReadyTime = null,
                            number = 15
                        )
                    }

                    if (expandedRecipes != null && expandedRecipes.isNotEmpty()) {
                        val fragment = RecipeListFragment().apply {
                            arguments = Bundle().apply {
                                putString("search_query", selectedIngredients.joinToString(","))
                                putString("cook_time_filter", "Any")
                                putString("diet_filter", diet ?: "")
                            }
                        }
                        (activity as? HomeActivity)?.navigateToFragment(fragment)
                    } else {
                        Toast.makeText(requireContext(), "No recipes found matching filters", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // RecyclerView and adapter are set up once ingredients are loaded in loadUserIngredients().
    }
    private fun loadUserIngredients() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve the active username from SharedPreferences.
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No username found", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            // Fetch the user's ingredients from Realm via HomeViewModel.
            val ingredients = homeViewModel.getIngredientsForUser(username)
            // Map Realm Ingredient objects to InventoryItem.
            userIngredients = ingredients.map { ingredient ->
                InventoryItem(
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit
                )
            }
            withContext(Dispatchers.Main) {
                setupRecyclerView()
            }
        }
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with the user's ingredients.
        adapter = FilterIngredientAdapter(userIngredients) { selectedItems ->
            // Optional: update UI to reflect the current number of selected ingredients.
        }
        // Note: Ensure your RecyclerView in your XML has the ID "recyclerViewFilters".
        binding.recyclerViewFilters.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFilters.adapter = adapter

        // If you have an "Apply Filters" button in your layout (e.g. with ID buttonApplyFilters),
        // set its click listener here.
        binding.buttonSearchByIngredients.setOnClickListener {
            val selectedIngredients = adapter.getSelectedIngredients()
            // Create a comma-separated query from the selected ingredient names.
            val searchQuery = selectedIngredients.joinToString(",") { it.name }
            // Navigate to RecipeListFragment with the search query.
            val fragment = RecipeListFragment().apply {
                arguments = Bundle().apply {
                    putString("search_query", searchQuery)
                }
            }
            (activity as? HomeActivity)?.navigateToFragment(fragment)
        }
    }
}
