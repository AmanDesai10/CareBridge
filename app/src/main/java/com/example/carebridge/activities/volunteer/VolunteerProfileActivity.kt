package com.example.carebridge.activities.volunteer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carebridge.R
import com.example.carebridge.models.User
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * This activity displays the volunteer's profile information.
 */
class VolunteerProfileActivity : AppCompatActivity() {

    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var name: TextView
    private lateinit var age: TextView
    private lateinit var gender: TextView
    private lateinit var address: TextView
    private lateinit var headerTv: TextView
//    private lateinit var profilePhoto: ImageView
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private lateinit var sharedPreferncesManager: SharedPreferncesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_profile)

        headerTv = findViewById(R.id.headerTV)
        username = findViewById(R.id.usernameTextView)
        email = findViewById(R.id.emailTextView)
        phoneNumber = findViewById(R.id.phoneNumberTextView)
        name = findViewById(R.id.nameTextView)
        age = findViewById(R.id.ageTextView)
        gender = findViewById(R.id.genderTextView)
        address = findViewById(R.id.addressTextView)
//        profilePhoto = findViewById(R.id.profilePhotoImageView)

        val editButton: Button = findViewById(R.id.editButton)
        headerTv.text = "Volunteer Profile"
        editButton.setOnClickListener {
            val intent = Intent(this, VolunteerEditProfileActivity::class.java)
            startActivity(intent)
        }
        sharedPreferncesManager = SharedPreferncesManager(this)
        val userId = sharedPreferncesManager.getValue("id")
        lifecycleScope.launch {
            val user: User? = getUserById(userId.toString())
            if (user != null) {
                name.text = user.name
                username.text = user.username
                email.text = user.email
                phoneNumber.text = user.phoneNumber
                age.text = user.age
                gender.text = user.gender
                address.text = user.address

                saveUserDetails(user)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when activity is resumed
        refreshProfileData()
    }

    /**
     * Refreshes the profile data from SharedPreferences and updates UI.
     */
    private fun refreshProfileData() {
        // Retrieve user details from SharedPreferences and update UI
        name.text = sharedPreferncesManager.getValue("name")
        username.text = sharedPreferncesManager.getValue("username")
        email.text = sharedPreferncesManager.getValue("email")
        phoneNumber.text = sharedPreferncesManager.getValue("phoneNumber")
        age.text = sharedPreferncesManager.getValue("age")
        gender.text = sharedPreferncesManager.getValue("gender")
        address.text = sharedPreferncesManager.getValue("address")
    }

    /**
     * Retrieves user by ID from Firestore.
     *
     * @param userId The user ID.
     * @return The user details if found, null otherwise.
     */
    private suspend fun getUserById(userId: String): User? {
        // Reference to the document with the specified ID
        val userDocRef = usersCollection.document(userId)

        return try {
            // Fetch data from the document asynchronously
            val documentSnapshot = userDocRef.get().await()

            // Convert document snapshot to User object
            val user = documentSnapshot.toObject(User::class.java)

            // Return the user object
            user
        } catch (e: Exception) {
            // Handle any errors
            null
        }
    }

    /**
     * Saves user details in SharedPreferences.
     *
     * @param user The user whose details are to be saved.
     */
    private fun saveUserDetails(user: User) {
        // Save user details in SharedPreferences
        sharedPreferncesManager.saveKey("name", user.name)
        sharedPreferncesManager.saveKey("username", user.username)
        sharedPreferncesManager.saveKey("email", user.email.toString())
        sharedPreferncesManager.saveKey("phoneNumber", user.phoneNumber.toString())
        sharedPreferncesManager.saveKey("age", user.age.toString())
        sharedPreferncesManager.saveKey("gender", user.gender.toString())
        sharedPreferncesManager.saveKey("address", user.address.toString())
    }
}