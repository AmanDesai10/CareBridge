package com.example.carebridge.activities.seniorcitizen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.carebridge.R

class SeniorFirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senior1)
        val textView: TextView = findViewById(R.id.SeniorTextView)


        textView.text = "Senior Citizen text view"
    }
}