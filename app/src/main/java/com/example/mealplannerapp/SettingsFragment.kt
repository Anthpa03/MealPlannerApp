package com.example.mealplannerapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.mealplannerapp.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = SharedPreferencesManager.getUsername(requireContext())
        binding.usernameTextView.text = username

        binding.editProfileImageButton.setOnClickListener {
            (activity as? HomeActivity)?.navigateToFragment(EditProfileFragment())
        }
        binding.aboutImageButton.setOnClickListener {
            (activity as? HomeActivity)?.navigateToFragment(AboutFragment())
        }
        binding.logoutImageButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Logout") { _, _ ->
            SharedPreferencesManager.clearUserCredentials(requireContext())
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}