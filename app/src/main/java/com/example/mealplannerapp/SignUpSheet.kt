package com.example.mealplannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SignUpSheet : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
    }

    //Handles the user's selection of sign up request
    fun signInText (view: View) {
        // Handler code here.
        val changePage = Intent(this@SignUpSheet, MainActivity::class.java)
        startActivity(changePage);
    }
}