package com.example.mealplannerapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplannerapp.databinding.FragmentCalendarBinding
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker


class CalendarFragment : BaseFragment<FragmentCalendarBinding>(FragmentCalendarBinding::inflate) {
    private lateinit var adapter: RecipeAdapter
    //private var savedRecipeList = mutableListOf<RecipeSearch.RecipeDisplayInfo>()
    private var savedRecipeList = mutableListOf(
        RecipeSearch.RecipeDisplayInfo("Spaghetti Bolognese", "https://example.com/spaghetti.jpg", "12 mins",0),
        RecipeSearch.RecipeDisplayInfo("Chicken Curry", "https://example.com/chicken_curry.jpg", "18 mins",0),
        RecipeSearch.RecipeDisplayInfo("Vegetable Stir Fry", "https://example.com/veg_stir_fry.jpg", "8 mins",0)
    )
    private var mealDatePicker: DayScrollDatePicker? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mealDatePicker = binding.mealDatePicker
        mealDatePicker?.getSelectedDate { date ->
            if (date != null) {
                Toast.makeText(requireContext(), "Current Date: $date", Toast.LENGTH_SHORT).show()
                //TODO:implement logic to show saved recipes based on date selected;
                // likely have to make a new function in RecipeAdapter for this
            }
        }
        setupRecyclerView()
        binding.textViewResults.text = "Showing ${savedRecipeList.size} ingredients"
    }

    private fun setupRecyclerView() {
        val activity = requireActivity() as HomeActivity
        adapter = RecipeAdapter(savedRecipeList, activity)
        binding.recyclerViewRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecipes.adapter = adapter
    }
}