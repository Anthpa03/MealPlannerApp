package com.example.mealplannerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mealplannerapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve and display username
        val username = SharedPreferencesManager.getUsername(requireContext())
        binding.textViewUsername.text = username

        // Navigate to RecipeListFragment when clicking EditTextSearch or the ImageButtonSearch
        binding.editTextSearch.setOnClickListener {
            navigateToRecipeList()
        }
        binding.imageButtonSearch.setOnClickListener{
            navigateToRecipeList()
        }
    }

    private fun navigateToRecipeList() {
        val searchQuery = binding.editTextSearch.text.toString().trim()
        val fragment = RecipeListFragment().apply {
            arguments = Bundle().apply {
                putString("search_query", searchQuery)
            }
        }

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}