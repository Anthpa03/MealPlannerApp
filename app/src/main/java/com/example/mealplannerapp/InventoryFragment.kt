package com.example.mealplannerapp

import InventoryAdapter
import InventoryItem
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.databinding.FragmentInventoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class InventoryFragment : BaseFragment<FragmentInventoryBinding>(FragmentInventoryBinding::inflate) {
    private lateinit var adapter: InventoryAdapter
    private lateinit var recyclerView: RecyclerView
    private val inventoryList = mutableListOf<InventoryItem>()
    private val homeViewModel: HomeViewModel by viewModels()
    private var ingredients = listOf(

        // Vegetables
        "Tomato", "Onion", "Garlic", "Carrot", "Potato", "Sweet Potato", "Bell Pepper",
        "Cucumber", "Zucchini", "Eggplant", "Spinach", "Kale", "Lettuce", "Cabbage",
        "Cauliflower", "Broccoli", "Celery", "Mushrooms", "Green Beans", "Asparagus",
        "Radish", "Beetroot", "Turnip", "Okra",

        // Fruits
        "Apple", "Banana", "Orange", "Lemon", "Lime", "Grapes", "Pineapple", "Mango",
        "Watermelon", "Papaya", "Strawberry", "Blueberry", "Raspberry", "Blackberry",
        "Pear", "Cherry", "Peach", "Plum", "Avocado", "Coconut",

        // Dairy & Eggs
        "Milk", "Cheese", "Butter", "Yogurt", "Cream", "Sour Cream", "Cottage Cheese",
        "Mozzarella", "Parmesan", "Feta Cheese", "Cheddar Cheese", "Goat Cheese", "Eggs",

        // Meat & Seafood
        "Chicken Breast", "Chicken Thighs", "Chicken Wings", "Ground Beef", "Steak",
        "Pork Chops", "Ground Pork", "Bacon", "Sausage", "Lamb", "Turkey", "Shrimp",
        "Salmon", "Tuna", "Cod", "Tilapia", "Crab", "Lobster", "Mussels",

        // Grains & Pasta
        "Rice", "Brown Rice", "White Rice", "Basmati Rice", "Quinoa", "Oats", "Barley",
        "Couscous", "Pasta", "Spaghetti", "Macaroni", "Bread", "Tortilla", "Pita Bread",

        // Nuts, Seeds & Legumes
        "Almonds", "Cashews", "Peanuts", "Walnuts", "Chia Seeds", "Flaxseeds",
        "Sunflower Seeds", "Lentils", "Chickpeas", "Black Beans", "Kidney Beans",

        // Spices & Condiments
        "Salt", "Black Pepper", "Paprika", "Chili Powder", "Cumin", "Coriander",
        "Oregano", "Thyme", "Rosemary", "Basil", "Cinnamon", "Nutmeg", "Cloves",
        "Vanilla Extract", "Soy Sauce", "Honey", "Mustard", "Ketchup", "Mayonnaise",
        "Olive Oil", "Vinegar"
    )
    private var units = listOf(
        "Teaspoon (tsp)", "Tablespoon (tbsp)", "Cup", "Fluid Ounce (fl oz)",
        "Pint (pt)", "Quart (qt)", "Gallon (gal)", "Milliliter (ml)", "Liter (l)",
        "Ounce (oz)", "Pound (lb)", "Gram (g)", "Kilogram (kg)"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerViewIngredients
        setupRecyclerView()
        loadInventoryData()
        binding.textViewResults.text = "Showing ${inventoryList.size} ingredients"

        binding.buttonAddIngredient.setOnClickListener {
            showBottomDialog()
        }

        (activity as? HomeActivity)?.setupSpinner(binding.spinnerSortIngredient) { position ->
            when (position) {
                0 -> sortIngredients("default")
                1 -> sortIngredients("ascending")
                2 -> sortIngredients("descending")
            }
        }
    }

    private fun sortIngredients(order: String) {
        when (order) {
            "ascending" -> {
            }

            "descending" -> {
            }

            else -> {
                // Default for this one should also be prioritizing bookmarks
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(inventoryList) { ingredient ->
            // Find position of the ingredient
            val position = inventoryList.indexOf(ingredient)
            if (position != -1) {
                showBottomDialog(
                    existingIngredient = ingredient.name,
                    existingQuantity = ingredient.quantity,
                    existingUnit = ingredient.unit,
                    position = position
                )
            }
        }
        binding.recyclerViewIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIngredients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InventoryFragment.adapter
        }
    }

    private fun showBottomDialog(
        existingIngredient: String? = null,
        existingQuantity: String? = null,
        existingUnit: String? = null,
        position: Int? = null
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_dialog_add_ingredients)

        val autoCompleteIngredient = dialog.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_ingredient)
        val editTextQuantity = dialog.findViewById<EditText>(R.id.editText_quantity)
        val autoCompleteUnit = dialog.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_unit)
        val buttonAddItem = dialog.findViewById<Button>(R.id.button_change_username)
        val buttonRemoveItem = dialog.findViewById<Button>(R.id.button_add_recipe)
        val buttonExit = dialog.findViewById<ImageButton>(R.id.cancelButton)

        // Initially disable quantity and unit input.
        editTextQuantity.isEnabled = false
        autoCompleteUnit.isEnabled = false

        val ingredientsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients)
        autoCompleteIngredient.setAdapter(ingredientsAdapter)
        val unitsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        autoCompleteUnit.setAdapter(unitsAdapter)

        // Determine if this dialog is for editing an existing ingredient.
        if (existingIngredient != null) {
            // Prefill fields for editing.
            autoCompleteIngredient.setText(existingIngredient, false)
            autoCompleteIngredient.isEnabled = false
            editTextQuantity.isEnabled = true
            autoCompleteUnit.isEnabled = true
            // Change the primary button text to "Edit".
            buttonAddItem.text = "Edit"
            // Make the remove button visible when editing.
            buttonRemoveItem.visibility = View.VISIBLE
        } else {
            // Hide the remove button when adding a new ingredient.
            buttonRemoveItem.visibility = View.GONE
        }
        if (existingQuantity != null) editTextQuantity.setText(existingQuantity)
        if (existingUnit != null) autoCompleteUnit.setText(existingUnit, false)

        // Enable quantity input once an ingredient is selected.
        autoCompleteIngredient.setOnItemClickListener { _, _, _, _ ->
            editTextQuantity.isEnabled = true
        }

        // Enable unit input only if some quantity is entered.
        editTextQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                autoCompleteUnit.isEnabled = !s.isNullOrEmpty()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Primary "Add" or "Edit" button action.
        buttonAddItem.setOnClickListener {
            val ingredient = autoCompleteIngredient.text.toString().trim()
            val quantity = editTextQuantity.text.toString().trim()
            val unit = autoCompleteUnit.text.toString().trim()

            if (ingredient.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a valid ingredient", Toast.LENGTH_SHORT).show()
            } else if (quantity.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the quantity", Toast.LENGTH_SHORT).show()
            } else {
                if (position != null) {
                    // Call update logic if editing.
                    updateIngredientInUI(ingredient, quantity, unit)
                    inventoryList[position] = InventoryItem(ingredient, quantity, unit)
                    adapter.notifyItemChanged(position)
                } else {
                    // Otherwise, add a new ingredient.
                    val unitFinal = if (unit.isEmpty()) "" else unit
                    saveIngredients(ingredient, quantity, unitFinal)
                    val newIngredient = InventoryItem(ingredient, quantity, unitFinal)
                    inventoryList.add(newIngredient)
                    adapter.notifyItemInserted(inventoryList.size - 1)
                    Toast.makeText(requireContext(), "Added: $quantity $unitFinal of $ingredient", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        // Remove button action.
        buttonRemoveItem.setOnClickListener {
            val ingredient = autoCompleteIngredient.text.toString().trim()
            if (ingredient.isNotEmpty()) {
                removeIngredientInUI(ingredient)
                val index = inventoryList.indexOfFirst { it.name.trim().equals(ingredient, ignoreCase = true) }
                if (index != -1) {
                    inventoryList.removeAt(index)
                    lifecycleScope.launch(Dispatchers.Main) {
                        adapter.notifyItemRemoved(index)
                    }
                }
            }
            dialog.dismiss()
        }

        buttonExit.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }




    private fun loadInventoryData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve the active username from SharedPreferences.
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                Log.e("InventoryFragment", "No username found in SharedPreferences!")
                return@launch
            }
            Log.d("InventoryFragment", "Loading ingredients for username: '$username'")

            // Use HomeViewModel to get the list of Ingredient objects for the user.
            val ingredientsFromRealm = homeViewModel.getIngredientsForUser(username)
            Log.d(
                "InventoryFragment",
                "Found ${ingredientsFromRealm.size} ingredients for user: '$username'"
            )

            // Clear current inventory list and convert each Ingredient to an InventoryItem.
            inventoryList.clear()
            ingredientsFromRealm.forEach { ingredient ->
                inventoryList.add(
                    InventoryItem(
                        ingredient.name,
                        ingredient.quantity,
                        ingredient.unit
                    )
                )
            }

            // Notify the adapter on the main thread.
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
            }
        }
    }


    private fun saveIngredients(ingredient: String, quantity: String, unit: String) {
        if (ingredient.isNotEmpty() && quantity.isNotEmpty() && unit.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                // Retrieve the username from SharedPreferences
                val username = SharedPreferencesManager.getUsername(requireContext())
                if (username.isNullOrEmpty()) {
                    Log.e("InventoryFragment", "No username found in SharedPreferences!")
                    return@launch
                } else {
                    Log.d("InventoryFragment", "Username retrieved: $username")
                }

                // Switch back to Main thread when accessing the ViewModel
                withContext(Dispatchers.Main) {
                    homeViewModel.addIngredient(
                        username,
                        ingredient,
                        quantity,
                        unit
                    )
                    Log.d(
                        "InventoryFragment",
                        "saveIngredients: addIngredient called on Main thread for username: $username"
                    )
                }
            }
        }
    }

    private fun updateIngredientInUI(ingredientName: String, newQuantity: String, newUnit: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Retrieve username from SharedPreferences.
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                Log.e("InventoryFragment", "No username found in SharedPreferences!")
                return@launch
            }
            // Call HomeViewModel updateIngredient method.
            homeViewModel.updateIngredient(username, ingredientName, newQuantity, newUnit)
            // Update the local list if needed.
            val index = inventoryList.indexOfFirst { it.name.trim().equals(ingredientName.trim(), ignoreCase = true) }
            if (index != -1) {
                inventoryList[index] = InventoryItem(ingredientName, newQuantity, newUnit)
                withContext(Dispatchers.Main) {
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }


    private fun removeIngredientInUI(ingredientName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val username = SharedPreferencesManager.getUsername(requireContext())
            if (username.isNullOrEmpty()) {
                Log.e("InventoryFragment", "No username found in SharedPreferences!")
                return@launch
            }
            // Call ViewModel method to remove the ingredient.
            homeViewModel.removeIngredient(username, ingredientName)
            // Optionally, update your local list.
            val index = inventoryList.indexOfFirst {
                it.name.trim().equals(ingredientName.trim(), ignoreCase = true)
            }
            if (index != -1) {
                inventoryList.removeAt(index)
                withContext(Dispatchers.Main) {
                    adapter.notifyItemRemoved(index)
                }
            }
        }
    }
}