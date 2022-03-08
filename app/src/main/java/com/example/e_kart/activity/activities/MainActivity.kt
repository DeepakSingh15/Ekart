package com.example.e_kart.activity.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.e_kart.R
import com.example.e_kart.utils.Constants
import com.example.e_kart.utils.MSPTextView
import com.example.e_kart.utils.MSPTextViewBold

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv_main = findViewById<MSPTextViewBold>(R.id.tv_main)
        val sharedPreferences = getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE)

        val username = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME, "")!!

        tv_main.text= "The logged in user is $username."
    }
}