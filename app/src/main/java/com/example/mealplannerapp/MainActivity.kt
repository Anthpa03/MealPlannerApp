package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mealplannerapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI
        initUI()
    }

    private fun initUI() {
        binding.buttonSignin.setOnClickListener {
            // Accessing user inputs
            val getUserEmail = binding.editTextEmailSignin
            val getUserPassword = binding.editTextPasswordSignin

        }
        binding.textViewSignupSheet.setOnClickListener{
            val changePage = Intent(this@MainActivity, SignUpSheet::class.java)
            startActivity(changePage);
        }
    }
}