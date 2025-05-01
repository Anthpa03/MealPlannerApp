package com.example.mealplannerapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentRecipeListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeListFragment : BaseFragment<FragmentRecipeListBinding>(FragmentRecipeListBinding::inflate) {

    private lateinit var adapter: RecipeAdapter
    private var fullRecipeList = listOf<RecipeSearch.RecipeDisplayInfo>()
    private var filteredRecipeList = mutableListOf<RecipeSearch.RecipeDisplayInfo>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        val searchQuery     = arguments?.getString("search_query") ?: ""
        val cookTimeFilter  = arguments?.getString("cook_time_filter")
        val dietFilter      = arguments?.getString("diet_filter")

        binding.editTextSearch.setText(searchQuery)

        // Show only spinner while we load
        binding.progressBar.visibility        = View.VISIBLE
        binding.recyclerViewRecipes.visibility = View.GONE
        binding.textViewResults.visibility     = View.GONE
        binding.spinnerSort.visibility         = View.GONE

        setupSearchListener()

        when {
            searchQuery.isEmpty() -> loadDefaultRecipes()
            !cookTimeFilter.isNullOrEmpty() || !dietFilter.isNullOrEmpty() ->
                fetchFilteredRecipesWithInfo(searchQuery, dietFilter, cookTimeFilter)
            else -> fetchRecipesWithInfo(searchQuery)
        }

        binding.imageButtonFilter.setOnClickListener {
            (activity as? HomeActivity)
                ?.navigateToFragment(FilterListFragment())
        }

        (activity as? HomeActivity)
            ?.setupSpinner(binding.spinnerSort) { position ->
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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun filterRecipes(query: String) {
        filteredRecipeList.apply {
            clear()
            if (query.isEmpty()) addAll(fullRecipeList)
            else addAll(fullRecipeList.filter { it.title.contains(query, ignoreCase = true) })
        }
        sortRecipes("default")
        adapter.notifyDataSetChanged()
        binding.textViewResults.text = "Showing ${filteredRecipeList.size} results"
    }

    private fun fetchRecipesWithInfo(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // 1️⃣ Show loading
            binding.progressBar.visibility        = View.VISIBLE
            binding.recyclerViewRecipes.visibility = View.GONE
            binding.textViewResults.visibility     = View.GONE
            binding.spinnerSort.visibility         = View.GONE

            // 2️⃣ Perform network call on IO
            val results = withContext(Dispatchers.IO) {
                RecipeSearch.searchDisplayInfoByQuery(query)
            }.orEmpty()

            // 3️⃣ Update lists & adapter
            fullRecipeList = results
            filteredRecipeList.apply {
                clear()
                addAll(results)
            }
            adapter.notifyDataSetChanged()

            // 4️⃣ Reveal UI
            binding.progressBar.visibility        = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
            binding.textViewResults.visibility     = View.VISIBLE
            binding.spinnerSort.visibility         = View.VISIBLE
            binding.textViewResults.text           = "Showing ${results.size} results"
        }
    }

    private fun fetchFilteredRecipesWithInfo(
        ingredientsQuery: String,
        dietFilter: String?,
        cookTimeFilter: String?
    ) {
        // 1) parse your time filters into ints
        val (minTime, maxTime) = when {
            cookTimeFilter?.contains("0-15") == true  -> 0 to 15
            cookTimeFilter?.contains("16-30") == true -> 16 to 30
            cookTimeFilter?.contains("31-60") == true -> 31 to 60
            cookTimeFilter?.contains(">60") == true   -> 61 to Int.MAX_VALUE
            else                                      -> null to null
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // ⚙️ Show loading
            binding.progressBar.visibility         = View.VISIBLE
            binding.recyclerViewRecipes.visibility = View.GONE
            binding.textViewResults.visibility     = View.GONE
            binding.spinnerSort.visibility         = View.GONE

            // 2) First network call with time filters
            var details = withContext(Dispatchers.IO) {
                RecipeSearch.searchRecipesWithInfoAndFilters(
                    query        = ingredientsQuery,
                    diet         = dietFilter,
                    minReadyTime = minTime,
                    maxReadyTime = maxTime
                )
            }.orEmpty()

            // 3) If *empty* and we did actually supply a range,
            //    show a toast then broaden by re-calling with no times
            if (details.isEmpty() && (minTime != null || maxTime != null)) {
                Toast.makeText(
                    requireContext(),
                    "No recipes within $cookTimeFilter; broadening search…",
                    Toast.LENGTH_SHORT
                ).show()

                details = withContext(Dispatchers.IO) {
                    RecipeSearch.searchRecipesWithInfoAndFilters(
                        query        = ingredientsQuery,
                        diet         = dietFilter,
                        minReadyTime = null,
                        maxReadyTime = null
                    )
                }.orEmpty()
            }

            // 4) Map to your RecyclerView model
            val display = details.map { d ->
                RecipeSearch.RecipeDisplayInfo(
                    title    = d.title,
                    imageUrl = d.image,
                    cookTime = "${d.readyInMinutes} mins",
                    recipeId = d.id
                )
            }

            // 5) Update adapter
            fullRecipeList = display
            filteredRecipeList.apply {
                clear()
                addAll(display)
            }
            adapter.notifyDataSetChanged()

            // 6) Reveal
            binding.progressBar.visibility         = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
            binding.textViewResults.visibility     = View.VISIBLE
            binding.spinnerSort.visibility         = View.VISIBLE
            binding.textViewResults.text           = "Showing ${display.size} recipes"
        }
    }


    private fun loadDefaultRecipes() {
        fullRecipeList = listOf(
            RecipeSearch.RecipeDisplayInfo("Cheese Pizza",    "https://example.com/pizza.jpg", "15 mins", 0),
            RecipeSearch.RecipeDisplayInfo("Pepperoni Pizza", "https://example.com/pizza.jpg", "20 mins", 0)
        )
        filterRecipes(binding.editTextSearch.text.toString())

        binding.progressBar.visibility        = View.GONE
        binding.recyclerViewRecipes.visibility = View.VISIBLE
        binding.textViewResults.visibility     = View.VISIBLE
        binding.spinnerSort.visibility         = View.VISIBLE
    }

    private fun sortRecipes(order: String) {
        val username = SharedPreferencesManager.getUsername(requireContext()) ?: return
        val sorted = when (order) {
            "ascending"  -> filteredRecipeList.sortedBy { it.title }
            "descending" -> filteredRecipeList.sortedByDescending { it.title }
            else         -> filteredRecipeList.sortedWith(
                compareByDescending<RecipeSearch.RecipeDisplayInfo> {
                    SharedPreferencesManager.isBookmarked(requireContext(), username, it.title)
                }.thenBy { it.title }
            )
        }
        filteredRecipeList.apply {
            clear()
            addAll(sorted)
        }
        adapter.notifyDataSetChanged()
    }
}
