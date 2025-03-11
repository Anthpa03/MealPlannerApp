package com.example.mealplannerapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.toTypedArray

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InventoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InventoryFragment : Fragment() {
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var ingredientquantity: TextInputLayout
    private lateinit var ingredientAutoComplete: AutoCompleteTextView
    private lateinit var unitspinner: Spinner
    private lateinit var CompleteButton: Button
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
        "Select Unit", "Teaspoon (tsp)", "Tablespoon (tbsp)", "Cup", "Fluid Ounce (fl oz)",
        "Pint (pt)", "Quart (qt)", "Gallon (gal)", "Milliliter (ml)", "Liter (l)",
        "Ounce (oz)", "Pound (lb)", "Gram (g)", "Kilogram (kg)"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inventory, container, false).apply {

            // Get references to the views
            unitspinner = findViewById<Spinner>(R.id.spinner)
            ingredientquantity = findViewById(R.id.Quantity)
            ingredientAutoComplete = findViewById(R.id.ingredientAutoComplete)
            CompleteButton = findViewById(R.id.button)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adapter for auto-completion
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients)

        unitspinner.adapter =ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ingredientAutoComplete.setAdapter(adapter)
        view.findViewById<Button>(R.id.button).setOnClickListener {
            SaveIngredients()
        }
        setupAutoComplete()
    }

    private fun SaveIngredients()
    {
     val getingredient = ingredientAutoComplete.text.toString()
     val getquantity = ingredientquantity.editText?.text.toString()
     val getunit = unitspinner.selectedItem.toString()

     if(getingredient.isNotEmpty() && getquantity.isNotEmpty() && getunit.isNotEmpty())
     {
         if (getingredient in ingredients)
         {
             lifecycleScope.launch(Dispatchers.IO) {
                 SharedPreferencesManager.getUsername(requireContext())
                     ?.let { homeViewModel.addIngredient(it, getingredient, getquantity, getunit) }
             }
         }
     }
    }
    private fun setupAutoComplete() {

        }
    }