package com.example.carebridge.activities.volunteer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.carebridge.R
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Activity for editing the volunteer's profile.
 */
class VolunteerEditProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferncesManager: SharedPreferncesManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var headerTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_edit_profile)

        // Initialize views
        headerTv = findViewById(R.id.headerTV)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        genderEditText = findViewById(R.id.genderEditText)
        addressEditText = findViewById(R.id.addressEditText)

        // Initialize SharedPreferncesManager and Firestore
        sharedPreferncesManager = SharedPreferncesManager(this)
        firestore = FirebaseFirestore.getInstance()

        headerTv.text = "Volunteer Profile"
        // Retrieve current user details from SharedPreferences
        val userId = sharedPreferncesManager.getValue("id")
        val name = sharedPreferncesManager.getValue("name")
        val username = sharedPreferncesManager.getValue("username")
        val email = sharedPreferncesManager.getValue("email")
        val phoneNumber = sharedPreferncesManager.getValue("phoneNumber")
        val age = sharedPreferncesManager.getValue("age")
        val gender = sharedPreferncesManager.getValue("gender")
        val address = sharedPreferncesManager.getValue("address")

        // Populate EditText fields with current user details
        usernameEditText.setText(username)
        emailEditText.setText(email)
        phoneNumberEditText.setText(phoneNumber)
        nameEditText.setText(name)
        ageEditText.setText(age)
        genderEditText.setText(gender)
        addressEditText.setText(address)

        // Save button
        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            // Retrieve updated data from EditText fields
            val updatedUsername = usernameEditText.text.toString()
            val updatedEmail = emailEditText.text.toString()
            val updatedPhoneNumber = phoneNumberEditText.text.toString()
            val updatedName = nameEditText.text.toString()
            val updatedAge = ageEditText.text.toString()
            val updatedGender = genderEditText.text.toString()
            val updatedAddress = addressEditText.text.toString()

            // Update SharedPreferences with the new data
            sharedPreferncesManager.saveKey("username", updatedUsername)
            sharedPreferncesManager.saveKey("email", updatedEmail)
            sharedPreferncesManager.saveKey("phoneNumber", updatedPhoneNumber)
            sharedPreferncesManager.saveKey("age", updatedAge)
            sharedPreferncesManager.saveKey("gender", updatedGender)
            sharedPreferncesManager.saveKey("address", updatedAddress)
            sharedPreferncesManager.saveKey("name", updatedName)

            // Update user document in Firestore collection
            val userRef = firestore.collection("users").document(userId.toString())
            GlobalScope.launch {
                try {
                    userRef.update(
                        mapOf(
                            "username" to updatedUsername,
                            "email" to updatedEmail,
                            "phoneNumber" to updatedPhoneNumber,
                            "age" to updatedAge,
                            "gender" to updatedGender,
                            "address" to updatedAddress
                        )
                    ).await()
                    // Data updated successfully in Firestore
                } catch (e: Exception) {
                    // Handle error
                    e.printStackTrace()
                }
            }
            // Optionally, navigate back to the profile activity
            finish()
        }
    }
}