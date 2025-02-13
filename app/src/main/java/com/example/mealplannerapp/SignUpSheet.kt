package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.mealplannerapp.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpSheet : AppCompatActivity(){

    private lateinit var binding: ActivitySignupBinding
    private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize View Binding
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI
        initUI()
    }

    private fun initUI() {
        binding.buttonSignup.setOnClickListener {
            // Accessing user inputs
            val getUsername = binding.editTextUsernameSignup.text.toString()
            val getUserPassword = binding.editTextPasswordSignup.text.toString()

            // Validate inputs
            if (validateInputs(getUsername, getUserPassword)) {
                // If inputs are valid, add user through ViewModel
                homeViewModel.updateName(getUsername)
                homeViewModel.updatePassword(getUserPassword)
                homeViewModel.insertUser()

                val changePage = Intent(this@SignUpSheet, MainActivity::class.java)
                startActivity(changePage)
            }
        }
        binding.textViewSigninSheet.setOnClickListener{
            val changePage = Intent(this@SignUpSheet, MainActivity::class.java)
            startActivity(changePage)
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.editTextUsernameSignup.error = "A Username is required"
            return false
        }
        if (password.length < 6) { //TODO:Add more password validation parameters
            binding.editTextPasswordSignup.error = "Password must be at least 6 characters"
            return false
        }
        if (!binding.editTextPasswordRetype.text.toString().equals(password)) {
            binding.editTextPasswordRetype.error = "Passwords must match"
            return false
        }
        return true
    }
}