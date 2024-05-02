package com.example.carebridge.activities.familyandfriends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.activities.shared.LoginActivity
import com.example.carebridge.activities.shared.ProfileActivity
import com.example.carebridge.adapters.SeniorCitizenAdapter
import com.example.carebridge.models.User
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

/**
 * This class represents the main activity for the Family and Friends view.
 * It displays a list of senior citizens connected to the family member.
 * It also provides options to add new accounts or link existing accounts.
 * It uses a Navigation Drawer for navigation between different views.
 */
class FamilyFriendsView : AppCompatActivity() {
    // Creating instances.
    private lateinit var firestore: FirebaseFirestore
    private lateinit var seniorCitizenList: ArrayList<User>
    private lateinit var sharedPreferncesManager: SharedPreferncesManager
    private lateinit var familyMemberRecyclerView: RecyclerView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_friends_views)

        // Initializing the instance for firestore, sharedPreferenceManager and recyclerView.
        firestore = FirebaseFirestore.getInstance()
        sharedPreferncesManager = SharedPreferncesManager(this)
        familyMemberRecyclerView = findViewById(R.id.familyMemberRecyclerView)
        val addNewAccountBtn: Button = findViewById(R.id.addAccBtn)
        val addExistingAccButton: Button = findViewById(R.id.linkAccBtn)

        familyMemberRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        getSeniorCitizenForFamilyMember(sharedPreferncesManager.getValue("id").toString())

        // Set click listeners for adding new senior citizen accounts
        addNewAccountBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("header", "Add User")
            startActivity(intent)
        }

        // Set click listeners for linking existing accounts.
        addExistingAccButton.setOnClickListener {
            val intent = Intent(this, ExistingSeniorCitizenView::class.java)
            startActivity(intent)
        }

        // Initialize DrawerLayout, ActionBarDrawerToggle, and NavigationView for the navigation drawer
        drawerLayout = findViewById(R.id.familyFriendsViewDrawerLayout)
        navigationView = findViewById(R.id.nav)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupNavigationView()
    }

    /**
     * Fetches senior citizens connected to the family member from the database.
     */
    private fun getSeniorCitizenForFamilyMember(familyMemberId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereIn("role", listOf("SENIOR_CITIZEN"))
            .whereArrayContains("familyMembers", familyMemberId)
            .get()
            .addOnSuccessListener { documents: QuerySnapshot? ->
                seniorCitizenList = ArrayList<User>()
                for (document in documents!!) {
                    val user = document.toObject(User::class.java)
                    seniorCitizenList.add(user)
                }
                val adapter = SeniorCitizenAdapter(
                    seniorCitizenList,
                    sharedPreferncesManager.getValue("id").toString(), false
                )
                familyMemberRecyclerView.adapter = adapter
            }
            .addOnFailureListener { e: Exception? ->
                Log.d("error", "Error getting documents.", e)
            }
    }

    /**
     * Sets up the NavigationView with item selected listener for navigation.
     */
    private fun setupNavigationView() {
        val role = "FAMILY_FRIENDS"

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.newAcc -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("header", "Add User")
                    startActivity(intent)
                    true
                }

                R.id.existingAcc -> {
                    val intent = Intent(this, ExistingSeniorCitizenView::class.java)
                    startActivity(intent)
                    true
                }

                R.id.familyProfile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("header", "Profile-Family")
                    startActivity(intent)
                    true
                }

                R.id.logout -> {
                    val authManager = AuthManager(this)
                    authManager.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    /**
     * Handles the action bar's up button click to toggle the navigation drawer.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}