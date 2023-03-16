package com.example.weatherapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weatherapi.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_holder, MainFragment()).commit()
    }
}