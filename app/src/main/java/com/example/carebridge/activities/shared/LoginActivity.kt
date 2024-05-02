package com.example.carebridge.activities.shared

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.app.Activity
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.messaging.FirebaseMessaging
import com.example.carebridge.activities.seniorcitizen.StoreHealthRecordActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.carebridge.R
import com.example.carebridge.activities.familyandfriends.FamilyFriendsView
import com.example.carebridge.activities.seniorcitizen.SeniorItemsListActivity
import com.example.carebridge.activities.volunteer.ClientConfirmation
import com.example.carebridge.services.falldetection.FallDetectionService
import com.example.carebridge.utils.AppLaunchChecker
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.UserRoles
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var appLaunchChecker: AppLaunchChecker

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var roleSpinner: Spinner
    private lateinit var registrationText: TextView
    private lateinit var loginErrorText: TextView
    private lateinit var authManager: AuthManager
    private lateinit var selectedRole: String
    var hasNotificationPermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        setContentView(R.layout.activity_login)
        authManager = AuthManager(this)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)
        roleSpinner = findViewById(R.id.spinnerRole)
        registrationText = findViewById(R.id.textViewRegister)
        loginErrorText = findViewById(R.id.loginError)

        // Check if user is already logged in
        if(authManager.isUserLoggedIn()) {
            Log.d("Role", authManager.getUserRole() ?: "No role")
            redirectToActivity(authManager.getUserRole() ?: "")
            return
        }

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

        // Handle registration text click
        registrationText.setOnClickListener {
            redirectToRegistration()
        }

        // Handle login button click
        loginButton.setOnClickListener {
            login()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun login() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        if (email == "" || password == "") {
            showError("Invalid email or password.")
            return
        }
        authManager.signIn(email, password, selectedRole) { message ->
            if (message == "Success") {
                // Redirect to appropriate activity based on user role
                redirectToActivity(selectedRole)
                // Set FCM token for messaging
                authManager.setFCMToke()
            }

            showError(message)
        }
    }

    // Function to retrieve and set FCM token
    private fun setFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Token retrieval successful, log or send to server
                Log.d("FCM Token", token ?: "Token is null")
            } else {
                // Token retrieval failed
                Log.e("FCM Token", "Failed to retrieve FCM token: ${task.exception}")
            }
        }
    }

    // Redirect to registration activity
    private fun redirectToRegistration() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun redirectToActivity(selectedRole: String) {
        // Redirect based on selected role
        if (selectedRole == UserRoles.VOLUNTEER.name) {
            val intent = Intent(this, ClientConfirmation::class.java)
            startActivity(intent)
        } else if (selectedRole == UserRoles.FAMILY_FRIENDS.name) {
            val intent = Intent(this, FamilyFriendsView::class.java)
            startActivity(intent)
        } else if (selectedRole == UserRoles.SENIOR_CITIZEN.name) {
            startFallDetectionService()
            redirectToStoreHealthRecordActivity()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Redirect to store health record activity
    private fun redirectToStoreHealthRecordActivity() {
        val intent = Intent(this, StoreHealthRecordActivity::class.java)
        startActivity(intent)
    }

    // Start fall detection service
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startFallDetectionService() {
        val intent = Intent(this, FallDetectionService::class.java)
        startService(intent)
        startForegroundService(intent)
    }

    // Show error message
    private fun showError(error: String) {
        loginErrorText.text = error
        loginErrorText.visibility = View.VISIBLE
    }

    // Request notification permission
    fun requestNotificationPermission(activity: Activity) {
        val notificationManager = NotificationManagerCompat.from(activity)
        if (!notificationManager.areNotificationsEnabled()) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Navigate to app settings
    private fun navigateToAppSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    // Request notification permission and handle result
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            }
        }

    // Show notification permission rationale
    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required to receive important updates.")
            .setPositiveButton("Allow") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Deny") { _, _ -> }
            .show()
    }

    // Show notification permission setting dialog
    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Notification Permission")
            .setMessage("Please allow notifications to receive important updates.")
            .setPositiveButton("Allow") { _, _ ->
                navigateToAppSettings(this)
            }
            .setNegativeButton("Deny") { _, _ -> }
            .show()
    }

    // Function to ask user to allow notification as overlay
    fun askToAllowNotificationAsOverlay(context: Context, callback: (Boolean) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_permissiion, null)
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(dialogView)
        val dialog = alertDialogBuilder.create()

        dialogView.findViewById<Button>(R.id.btnAllow)?.setOnClickListener {
            openNotificationSettings(context)
            dialog.dismiss()
            callback(true) // Notify that notification permission is allowed
        }

        dialogView.findViewById<Button>(R.id.btnDeny)?.setOnClickListener {
            dialog.dismiss()
            callback(false) // Notify that notification permission is denied
        }

        dialog.show()
    }

    // Open notification settings
    private fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
}