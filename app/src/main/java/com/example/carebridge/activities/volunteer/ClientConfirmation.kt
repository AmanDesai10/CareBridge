package com.example.carebridge.activities.volunteer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.activities.shared.LoginActivity
import com.example.carebridge.adapters.ClientConfirmationAdapter
import com.example.carebridge.models.ClientBookingConfirmation
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ClientConfirmation will be representing the bookings that are made by a client for specific volunteer.
 */
class ClientConfirmation : AppCompatActivity() {
    private lateinit var adapter: ClientConfirmationAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferncesManager: SharedPreferncesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_confirmation)
        sharedPreferncesManager = SharedPreferncesManager(this)


        /**
         * Initializing recyclerview and its adapter
         */
        val volunteerRecyclerView: RecyclerView = findViewById(R.id.volunteerRecyclerView)
        volunteerRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val clientBookingConfirmationList: ArrayList<ClientBookingConfirmation> = ArrayList()

        adapter = ClientConfirmationAdapter(clientBookingConfirmationList)
        volunteerRecyclerView.adapter = adapter

        /**
         * Connecting with the sharedPreferenceManager
         */
        confirmationDetails(sharedPreferncesManager)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawerLayout = findViewById(R.id.clientConfirmationDrawerLayout)
        navigationView = findViewById(R.id.nav_volunteer)

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
        navigationView.menu.getItem(0).isChecked = true

    }

    override fun onResume() {
        super.onResume()
        navigationView.menu.getItem(0).isChecked = true
        confirmationDetails(sharedPreferncesManager)
    }

    /**
     * Function to confirm the details that are fetched from the Firebase with the id present inside the sharedPrefernceManager
     * and populating the details in the form of cardview through the adapter
     */
    private fun confirmationDetails(sharedPreferncesManager: SharedPreferncesManager) {
        val db = FirebaseFirestore.getInstance()
        val clientBookingConfirmationList = ArrayList<ClientBookingConfirmation>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = sharedPreferncesManager.getValue("id")
                val userIdList = listOf(userId)

                /**
                 * Getting the client id from the volunteer_availability collection
                 */
                val querySnapshot = db.collection("volunteer_availability")
                    .whereIn("userId", userIdList) // Use the list here
                    .get().await()
                var bookedBy: ArrayList<String> = ArrayList()
                var bookingDate: String = ""

                /**
                 * Getting the user id from the collection
                 */
                for (document in querySnapshot.documents) {
                    val userId = document.getString("userId")
                    bookingDate = document.getString("date").toString()
                    bookedBy.add(document.getString("bookedBy").toString())
                }

                /**
                 * verifying the bookedBy id and the id present in the users collection
                 */
                val seniorCitizenQuerySnapshot = db.collection("users")
                    .whereIn("id", bookedBy) // Use the list here
                    .get().await()

                var clientName: String = ""
                var clientAddress: String = ""
                var clientPhoneNumber: String = ""
                var clientEmail: String = ""
                for (document in seniorCitizenQuerySnapshot.documents) {
                    Log.d("Document", document.id)
                    val userId = document.getString("id")
                    clientName = document.getString("name").toString()
                    clientAddress = document.getString("address").toString()
                    clientPhoneNumber = document.getString("phoneNumber").toString()
                    clientEmail = document.getString("email").toString()

                    val clientBookingConfirmation = ClientBookingConfirmation(
                        bookingDate,
                        clientName,
                        clientAddress,
                        clientPhoneNumber,
                        clientEmail
                    )
                    clientBookingConfirmationList.add(clientBookingConfirmation)
                }

                withContext(Dispatchers.Main) {
                    adapter.updateList(clientBookingConfirmationList)
                }
            } catch (e: Exception) {
                Log.d("", "Error getting documents from volunteer_availability.", e)
            }
        }
    }

    /**
     * Navigating to activities based on the selected item
     */
    private fun setupNavigationView() {

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.slotAvailability -> {
                    val intent = Intent(this, VolunteerSlotAvailabilityActivity::class.java)
                    intent.putExtra("header", "Add User")
                    startActivity(intent)
                    true
                }

                R.id.editProfile -> {
                    val intent = Intent(this, VolunteerEditProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.volunteerProfile -> {
                    val intent = Intent(this, VolunteerProfileActivity::class.java)
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
     * Adding toggle function on the item selected.
     * @param item param is used with reference of MenuItem, where on selecting a particular item from nav menu present, it will be redirected to that item.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}