package com.example.mealplannerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.mealplannerapp.databinding.FragmentFiltersBinding

class FilterListFragment : Fragment() {

    private lateinit var binding:FragmentFiltersBinding
    private var spinnerItems = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFiltersBinding.inflate(inflater, container, false)

        // Initializes the items on the time-to-cook spinner
        spinnerItems = arrayListOf("Any time", "15 Minutes", "30 Minutes")
        val spinnerAdapterTtc = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        spinnerAdapterTtc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ttcSpinner.adapter = spinnerAdapterTtc

        // Returns to previous menu
        binding.imageButtonExit.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }
}