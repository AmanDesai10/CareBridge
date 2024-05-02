package com.example.carebridge.activities.shared

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.carebridge.R
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.SharedPreferncesManager

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesManager: SharedPreferncesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        sharedPreferencesManager = SharedPreferncesManager(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun logout() {
        val authManager = AuthManager(this)
        authManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}