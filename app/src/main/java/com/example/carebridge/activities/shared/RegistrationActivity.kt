package com.example.carebridge.activities.shared

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.carebridge.R
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.UserRoles

class RegistrationActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextGender: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var signUpButton: Button
    private lateinit var roleSpinner: Spinner
    private lateinit var loginText: TextView
    private lateinit var selectedRole: String
    private lateinit var signUpErrorText: TextView
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // Initialize UI components
        editTextName = findViewById(R.id.editTextRegName)
        editTextUsername = findViewById(R.id.editTextRegUsername)
        editTextEmail = findViewById(R.id.editTextRegEmail)
        editTextPhoneNumber = findViewById(R.id.editTextRegPhoneNumber)
        editTextPassword = findViewById(R.id.editTextRegPassword)
        editTextAge = findViewById(R.id.editTextRegAge)
        editTextGender = findViewById(R.id.editTextRegGender)
        editTextAddress = findViewById(R.id.editTextRegAddress)
        signUpButton = findViewById(R.id.buttonSignUp)
        roleSpinner = findViewById(R.id.spinnerRegRole)
        loginText = findViewById(R.id.textViewLogin)
        signUpErrorText = findViewById(R.id.signUpError)

        // Populate spinner with user roles
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, UserRoles.getRoles())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        // Handle spinner item selection
        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRole = UserRoles.entries[position].name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Handle click on login text
        loginText.setOnClickListener {
            redirectToLogin()
        }

        // Handle click on sign-up button
        signUpButton.setOnClickListener {
            signUp()
        }
    }

    // Function to perform sign-up
    private fun signUp() {
        val name = editTextName.text.toString()
        val username = editTextUsername.text.toString()
        val email = editTextEmail.text.toString()
        val phoneNumber = editTextPhoneNumber.text.toString()
        val password = editTextPassword.text.toString()
        val age = editTextAge.text.toString()
        val gender = editTextGender.text.toString()
        val address = editTextAddress.text.toString()

        // Check if required fields are filled
        if (name == "" || username == "" || email == "" || password == "") {
            showError("Please fill in all the details.")
            return
        }

        // Register user with provided details
        authManager.registerUser(
            name,
            email,
            username,
            phoneNumber,
            age,
            gender,
            address,
            password,
            selectedRole,
            ArrayList()
        ) { message ->
            if (message == "Success") {
                redirectToLogin()
            }

            showError(message)
        }
    }

    // Function to show error message
    private fun showError(message: String) {
        signUpErrorText.text = message
        signUpErrorText.visibility = View.VISIBLE
    }

    // Function to redirect to login activity
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}