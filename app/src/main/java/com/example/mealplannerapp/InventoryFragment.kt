package com.example.mealplannerapp

import InventoryAdapter
import InventoryItem
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.databinding.FragmentInventoryBinding

class InventoryFragment : Fragment() {
    private lateinit var binding: FragmentInventoryBinding
    private lateinit var adapter: InventoryAdapter
    private lateinit var recyclerView: RecyclerView
    private val inventoryList = mutableListOf<InventoryItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewIngredients
        setupRecyclerView()
        loadInventoryData()
        binding.textViewResults.text = "Showing ${inventoryList.size} ingredients"

        binding.buttonAddIngredient.setOnClickListener {
            showBottomDialog()
        }

        // Initialize sorting options for spinner
        val sortOptions = listOf("Default", "Quantity Ascending", "Quantity Descending")
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.spinnerSort.adapter = spinnerAdapter

        // Handle sorting selection
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as TextView).text = null
                when (position) {
                    0 -> sortIngredients("default")
                    1 -> sortIngredients("ascending")
                    2 -> sortIngredients("descending")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return binding.root
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
        adapter = InventoryAdapter(inventoryList) {}
        binding.recyclerViewIngredients.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIngredients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InventoryFragment.adapter
        }
    }

    private fun showBottomDialog(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_dialog_add_ingredients)

        val autoCompleteIngredient = dialog.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_ingredient)
        val editTextQuantity = dialog.findViewById<EditText>(R.id.editText_quantity)
        val buttonAddItem = dialog.findViewById<Button>(R.id.button_add_item)
        val buttonExit = dialog.findViewById<ImageButton>(R.id.cancelButton)
        editTextQuantity.isEnabled = false

        // Sample ingredient list for testing autocomplete functionality
        val ingredients = listOf("Tomato", "Onion", "Garlic", "Carrot", "Spinach")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients)
        autoCompleteIngredient.setAdapter(adapter)

        // Enables quantity input once an ingredient has been selected
        autoCompleteIngredient.setOnItemClickListener { _, _, _, _ ->
            editTextQuantity.isEnabled = true
        }

        buttonAddItem.setOnClickListener {
            val selectedIngredient = autoCompleteIngredient.text.toString().trim()
            val quantity = editTextQuantity.text.toString().trim()

            if (selectedIngredient.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a valid ingredient", Toast.LENGTH_SHORT).show()
            } else if (quantity.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter the quantity", Toast.LENGTH_SHORT).show()
            } else {
                //TODO:add logic for storing ingredients to db
                Toast.makeText(requireContext(), "Added: $quantity of $selectedIngredient", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        buttonExit.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }

    private fun loadInventoryData() {
        //TODO: replace with real data
        inventoryList.addAll(
            listOf(
                InventoryItem("Tomato", 2),
                InventoryItem("Milk", 5),
                InventoryItem("Eggs", 12)
            )
        )
        adapter.notifyDataSetChanged()  // Refresh RecyclerView
    }
}