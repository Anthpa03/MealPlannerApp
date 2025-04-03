package com.example.mealplannerapp

import InventoryItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplannerapp.databinding.FilterIngredientListItemBinding

class FilterIngredientAdapter(
    private val ingredients: List<InventoryItem>,
    private val onSelectionChanged: (selectedItems: List<InventoryItem>) -> Unit
) : RecyclerView.Adapter<FilterIngredientAdapter.FilterIngredientViewHolder>() {

    // Track selected ingredients.
    private val selectedIngredients = mutableSetOf<InventoryItem>()

    inner class FilterIngredientViewHolder(val binding: FilterIngredientListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem) {
            // Set the ingredient name.
            binding.textViewIngredientName.text = item.name

            // Assuming your layout displays a label and then the quantity and unit.
            binding.quantityTextView.text = "Qty: "
            binding.amountTextView.text = item.quantity
            binding.unitTextView.text = item.unit

            // Update the checkbox state based on selection.
            binding.checkBox.isChecked = selectedIngredients.contains(item)

            // When the checkbox is clicked, update the selected set.
            binding.checkBox.setOnClickListener {
                if (binding.checkBox.isChecked) {
                    selectedIngredients.add(item)
                } else {
                    selectedIngredients.remove(item)
                }
                onSelectionChanged(selectedIngredients.toList())
            }

            // Optionally, allow the entire card to be clickable to toggle selection.
            binding.root.setOnClickListener {
                binding.checkBox.isChecked = !binding.checkBox.isChecked
                if (binding.checkBox.isChecked) {
                    selectedIngredients.add(item)
                } else {
                    selectedIngredients.remove(item)
                }
                onSelectionChanged(selectedIngredients.toList())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterIngredientViewHolder {
        val binding = FilterIngredientListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterIngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    fun getSelectedIngredients(): List<InventoryItem> = selectedIngredients.toList()
}
