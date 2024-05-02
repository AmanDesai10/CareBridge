package com.example.carebridge.activities.shared

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.carebridge.R
import com.example.carebridge.activities.familyandfriends.FamilyFriendsView
import com.example.carebridge.activities.seniorcitizen.StoreHealthRecordActivity
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore

/**
 * This activity serves as a profile management screen.
 * It allows users to view and edit their profile information.
 * It also provides functionality to add new users or update existing user profiles.
 */
class ProfileActivity : AppCompatActivity() {

    // Creating instance of AuthManager, FirebaseFirestore, SharedPreferenceManager
    private lateinit var authManager: AuthManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferncesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        intent = intent
        val headerText = intent.getStringExtra("header") ?: ""

        // Initializing AuthManager, FirebaseFirestore, SharedPreferenceManager
        authManager = AuthManager(this)
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = SharedPreferncesManager(this)
        // Initialize UI elements
        val headerTextView: TextView = findViewById(R.id.headerTV)
        val nameEditText: EditText = findViewById(R.id.profileNameEditTxt)
        val usernameEditTxt: EditText = findViewById(R.id.profileUsernameEditTxt)
        val emailIdEditText: EditText = findViewById(R.id.profileEmailEditTxt)
        val phoneNumberEditText: EditText = findViewById(R.id.profilePhoneNumberEditTxt)
        val ageEditTxt: EditText = findViewById(R.id.profileAgeEditText)
        val genderEditText: EditText = findViewById(R.id.profileGenderEditText)
        val addressEditText: EditText = findViewById(R.id.profileAddressEditText)
        val passwordEditText: EditText = findViewById(R.id.profilePasswordEditText)
        val passwordTextView: TextView = findViewById(R.id.profilePasswordTextView)
        val saveBtn: Button = findViewById(R.id.saveBtn)
        val userID = sharedPreferences.getValue("id")

        if (headerText.split("-")[0] == "Profile") {
            // displays the profile information in editable format.
            passwordEditText.isVisible = false
            passwordTextView.isVisible = false
            headerTextView.text = headerText.split("-")[0]
            nameEditText.setText(sharedPreferences.getValue("name"))
            usernameEditTxt.setText(sharedPreferences.getValue("username"))
            emailIdEditText.setText(sharedPreferences.getValue("email"))
            phoneNumberEditText.setText(sharedPreferences.getValue("phoneNumber"))
            ageEditTxt.setText(sharedPreferences.getValue("age"))
            genderEditText.setText(sharedPreferences.getValue("gender"))
            addressEditText.setText(sharedPreferences.getValue("address"))

            saveBtn.setOnClickListener {

                val usersCollection = firestore.collection("users").document(userID.toString())
                usersCollection.update(
                    mapOf(
                        "name" to nameEditText.text.toString(),
                        "username" to usernameEditTxt.text.toString(),
                        "phoneNumber" to phoneNumberEditText.text.toString(),
                        "email" to emailIdEditText.text.toString(),
                        "age" to ageEditTxt.text.toString(),
                        "gender" to genderEditText.text.toString(),
                        "address" to addressEditText.text.toString(),
                        "password" to passwordEditText.text.toString()
                    )
                )
                // Navigate to FamilyFriendsView or StoreHealthRecordActivity based on header text
                if (headerText.split("-")[1] == "Family") {
                    val intent = Intent(this, FamilyFriendsView::class.java)
                    startActivity(intent)
                } else if (headerText.split("-")[1] == "Senior") {
                    val intent = Intent(this, StoreHealthRecordActivity::class.java)
                    startActivity(intent)
                }
            }

        } else {
            // displays form to add new senior citizen.
            val familyMemberList: ArrayList<String> = ArrayList()
            familyMemberList.add(userID.toString())


            headerTextView.text = headerText

            saveBtn.setOnClickListener {
                val name = nameEditText.text
                val username = usernameEditTxt.text
                val phoneNumber = phoneNumberEditText.text
                val email = emailIdEditText.text
                val age = ageEditTxt.text
                val gender = genderEditText.text
                val address = addressEditText.text
                val password = passwordEditText.text

                addUser(
                    name.toString(),
                    username.toString(),
                    email.toString(),
                    phoneNumber.toString(),
                    age.toString(),
                    gender.toString(),
                    address.toString(),
                    password.toString(),
                    familyMemberList
                )
            }
        }
    }

    /**
     * Function to add a new user to the system.
     * It validates the input and calls the AuthManager to register the user.
     * If registration is successful, it redirects to the FamilyFriendsView.
     */
    private fun addUser(
        name: String,
        username: String,
        email: String,
        phoneNumber: String,
        age: String,
        gender: String,
        address: String,
        password: String,
        familyMembersList: ArrayList<String>
    ) {
        //checks if name, username, email or password are empty.
        if (name == "" || username == "" || email == "" || password == "") {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_LONG).show()
        }
        Log.d("username", username)
        authManager.registerUser(
            name,
            email,
            username,
            phoneNumber,
            age,
            gender,
            address,
            password,
            "SENIOR_CITIZEN",
            familyMembersList
        ) { message ->
            Log.d("msg", message)
            if (message == "Success") {
                redirectToFamilFriendsView()
            }
        }
    }

    /**
     * Function to redirect to the FamilyFriendsView after successful user registration.
     */
    private fun redirectToFamilFriendsView() {
        val intent = Intent(this, FamilyFriendsView::class.java)
        startActivity(intent)
    }
}