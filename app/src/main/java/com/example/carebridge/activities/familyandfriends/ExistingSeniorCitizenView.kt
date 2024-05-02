package com.example.carebridge.activities.familyandfriends

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.adapters.SeniorCitizenAdapter
import com.example.carebridge.models.User
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

/**
 * This is the Kotlin class file for activity_existing_senior_citizen_view.xml.
 * This class will display the list of senior citizens registered in the application.
 * The family member can connect with the senior citizen and add it to the list of connected senior citizen.
 * It will create the FirebaseFirestore instance to get data from the firebase.
 * It will populate the list of senior citizens in the recycler view.
 * This screen also implements the search functionality which will search the senior citizen based on their email.
 */
class ExistingSeniorCitizenView : AppCompatActivity() {
    // Creating instances.
    lateinit var firestore: FirebaseFirestore
    private lateinit var seniorCitizenList: ArrayList<User>
    lateinit var sharedPreferncesManager: SharedPreferncesManager
    lateinit var seniorCitizenRecyclerView: RecyclerView
    lateinit var searchView: SearchView
    lateinit var adapter: SeniorCitizenAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_existing_senior_citizen_view)

        firestore = FirebaseFirestore.getInstance()
        sharedPreferncesManager = SharedPreferncesManager(this)
        seniorCitizenRecyclerView = findViewById(R.id.seniorCitizenRecyclerView)
        searchView = findViewById(R.id.search_bar)

        // Set RecyclerView layout manager
        seniorCitizenRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        seniorCitizenList = ArrayList<User>()
        adapter = SeniorCitizenAdapter(
            seniorCitizenList,
            sharedPreferncesManager.getValue("id").toString(),
            true,
        )

        // Fetch senior citizens from database which are not connected to current logged in  family member.
        getAllSeniorCitizen(sharedPreferncesManager.getValue("id").toString())

        // Setup search view.
        setupSearchView()
    }

    /**
     * Fetches all senior citizens from the database.
     */
    fun getAllSeniorCitizen(familyMemberId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereIn("role", listOf("SENIOR_CITIZEN"))
            .get()
            .addOnSuccessListener { documents: QuerySnapshot? ->
                for (document in documents!!) {
                    val user = document.toObject(User::class.java)
                    if (!user.familyMembers.contains(familyMemberId)) {
                        seniorCitizenList.add(user)
                    }
                }
                adapter = SeniorCitizenAdapter(
                    seniorCitizenList,
                    sharedPreferncesManager.getValue("id").toString(), true
                )
                seniorCitizenRecyclerView.adapter = adapter
            }
            .addOnFailureListener { e: Exception? ->
                Log.d("error", "Error getting documents.", e)
            }
    }

    /**
     * Sets up the SearchView to filter the list of senior citizens based on their email.
     */
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterUser(newText.toString())
                return true
            }
        })
    }

    /**
     * Filters the list of senior citizens based on the provided email and updates the RecyclerView.
     */
    fun filterUser(email: String) {
        val filteredUser: ArrayList<User> = ArrayList<User>()
        for (user in seniorCitizenList) {
            if (user.email.contains(email, ignoreCase = true) && !user.familyMembers.contains(
                    sharedPreferncesManager.getValue("id")
                )
            ) {
                filteredUser.add(user)
            }
        }
        adapter = SeniorCitizenAdapter(
            filteredUser,
            sharedPreferncesManager.getValue("id").toString(), true
        )
        seniorCitizenRecyclerView.adapter = adapter
    }
}