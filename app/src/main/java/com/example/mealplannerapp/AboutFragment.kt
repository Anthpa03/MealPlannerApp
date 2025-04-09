package com.example.mealplannerapp

import android.os.Bundle
import android.view.View
import com.example.mealplannerapp.databinding.FragmentAboutBinding

class AboutFragment : BaseFragment<FragmentAboutBinding>(FragmentAboutBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageButtonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}