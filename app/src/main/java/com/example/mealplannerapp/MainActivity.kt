package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //Handles the user's selection of sign up request
    fun signUpText (view:View) {
        val changePage = Intent(this@MainActivity, SignUpSheet::class.java)
        startActivity(changePage);
    }
}