package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mealplannerapp.databinding.ActivitySignupBinding

class SignUpSheet : AppCompatActivity(){

    private lateinit var binding: ActivitySignupBinding
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
            val getUserEmail = binding.editTextEmailSignup
            val getUserPassword = binding.editTextPasswordSignup

        }
        binding.textViewSignupSheet.setOnClickListener{
            val changePage = Intent(this@SignUpSheet, MainActivity::class.java)
            startActivity(changePage)
        }
    }
}