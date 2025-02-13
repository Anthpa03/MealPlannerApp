package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.mealplannerapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private val homeViewModel: HomeViewModel by viewModels()
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
            val getUsername = binding.editTextUsernameSignin.text.toString()
            val getUserPassword = binding.editTextPasswordSignin.text.toString()

            homeViewModel.updateName(getUsername)
            homeViewModel.updatePassword(getUserPassword)
            homeViewModel.authenticateUser()

            if (homeViewModel.errorMessage.value.isNotEmpty()) {
                // Show an error message
                Toast.makeText(this, homeViewModel.errorMessage.value, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_LONG).show()
                //TODO: Implement home page transition
                //val intent = Intent(this@MainActivity, HomeActivity::class.java)
                //startActivity(intent)
                //finish()
            }
        }
        binding.textViewSignupSheet.setOnClickListener{
            val changePage = Intent(this@MainActivity, SignUpSheet::class.java)
            startActivity(changePage)
        }
    }
}