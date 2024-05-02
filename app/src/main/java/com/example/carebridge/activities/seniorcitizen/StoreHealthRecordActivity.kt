package com.example.carebridge.activities.seniorcitizen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.activities.shared.LoginActivity
import com.example.carebridge.activities.shared.ProfileActivity
import com.example.carebridge.adapters.HealthRecordAdapter
import com.example.carebridge.models.HealthRecord
import com.example.carebridge.utils.AuthManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

/**
 * StoreHealthRecordActivity is an activity that allows the senior citizen to store their health records in PDF format.
 * The senior citizen can upload a PDF file from their device to Firebase Storage.
 * The activity displays the list of health records stored by the senior citizen.
 * The senior citizen can also delete a health record from the list.
 */
class StoreHealthRecordActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthRecordAdapter
    val healthRecords = ArrayList<HealthRecord>()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    private val storage = Firebase.storage
    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // pickPdfFile is a contract that allows the user to pick a PDF file from their device.
    // The contract is registered with the activity result API.
    private val pickPdfFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            if (result.resultCode == RESULT_OK) {

                result.data?.data?.let { pdfUri ->

                    val filename = DocumentFile.fromSingleUri(this, pdfUri)?.name

                    // Get a reference to the Firebase Storage location where you want to upload the file

                    val storageRef = storage.reference.child("users/$userId/pdfs/$filename")
                    val storageRefAll = storage.reference.child("users/$userId/pdfs")

                    // Upload the file to Firebase Storage
                    val uploadTask = storageRef.putFile(pdfUri)
                    val pickPdfHealthRecordButton: Button =
                        findViewById(R.id.pickPdfHealthRecordButton)

                    uploadTask.addOnProgressListener {
                        pickPdfHealthRecordButton.isEnabled = false
                        pickPdfHealthRecordButton.text = buildString {
                            // show progress in percentage
                            append("Uploading...")
                            append(" ")
                            append((100.0 * it.bytesTransferred / it.totalByteCount).toInt())
                            append("%")
                        }
                    }
                    // Register observers to listen for when the upload is successful or fails
                    uploadTask.addOnSuccessListener {
                        pickPdfHealthRecordButton.isEnabled = true
                        pickPdfHealthRecordButton.text = "Upload Record"
                        getAllRecords(storageRefAll, userId)
                        // Handle successful upload
                        Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener { exception ->
                        // Handle unsuccessful upload
                        Toast.makeText(this, "Error uploading file: $exception", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                // Handle error or user cancellation
                Toast.makeText(this, "Error picking PDF", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * onCreate is called when the activity is starting.
     * It sets the content view of the activity to the layout defined in activity_store_health_record.xml.
     * It initializes the RecyclerView and the HealthRecordAdapter.
     * It fetches the health records stored by the senior citizen from Firebase Storage.
     * It sets an onClickListener on the pickPdfHealthRecordButton to allow the senior citizen to pick a PDF file.
     * It sets up the navigation drawer and the navigation view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_health_record)

        // Initialize the RecyclerView and the HealthRecordAdapter
        recyclerView = findViewById(R.id.pdfHealthRecordRecyclerView)
        adapter = HealthRecordAdapter(healthRecords, this@StoreHealthRecordActivity)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch the health records stored by the senior citizen from Firebase Storage
        fetchHealthRecordsFromFirestore()

        // Set an onClickListener on the pickPdfHealthRecordButton to allow the senior citizen to pick a PDF file
        val pickPdfHealthRecordButton: Button = findViewById(R.id.pickPdfHealthRecordButton)
        pickPdfHealthRecordButton.setOnClickListener {
            pickPdf()
        }

        // Set up the navigation drawer and the navigation view
        drawerLayout = findViewById(R.id.storeHealthRecordDrawerLayout)
        navigationView = findViewById(R.id.nav_senior)

        // Set up the action bar
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
        // Set the first item in the navigation view as checked
        navigationView.menu.getItem(0).isChecked = true
    }

    override fun onResume() {
        super.onResume()
        // set the first item in the navigation view as checked
        navigationView.menu.getItem(0).isChecked = true
    }

    /**
     * setupNavigationView sets up the navigation view.
     * It sets an onNavigationItemSelectedListener on the navigation view to handle the click events on the menu items.
     * It starts the corresponding activity when a menu item is clicked.
     * It logs out the user when the logout menu item is clicked.
     */
    private fun setupNavigationView() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.storeHealthRecord -> {
                    // starts the StoreHealthRecordActivity when the Store Health Record menu item is clicked
                    val intent = Intent(this, StoreHealthRecordActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.seniorItemsList -> {
                    // starts the SeniorItemsListActivity when the Senior Items List menu item is clicked
                    val intent = Intent(this, SeniorItemsListActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.bookVolunteer -> {
                    // starts the SeniorBookVolunteerActivity when the Book Volunteer menu item is clicked
                 val intent = Intent(this, SeniorBookVolunteerActivity::class.java)
                 startActivity(intent)
                 true
                }

                R.id.userReminder -> {
                    // starts the UserReminderActivity when the User Reminder menu item is clicked
                    val intent = Intent(this, UserReminder::class.java)
                    startActivity(intent)
                    true
                }

                R.id.seniorProfile -> {
                    // starts the ProfileActivity with the header "Profile-Senior" when the Senior Profile menu item is clicked
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("header", "Profile-Senior")
                    startActivity(intent)
                    true
                }

                R.id.logout -> {
                    // logs out the user when the Logout menu item is clicked
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
     * onOptionsItemSelected is called when an item in the options menu is selected.
     * It returns true if the item is selected and false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * fetchHealthRecordsFromFirestore fetches the health records stored by the senior citizen from Firebase Storage.
     * It gets the user ID of the senior citizen from Firebase Authentication.
     * It gets a reference to the Firebase Storage location where the health records are stored.
     * It fetches the health records from Firebase Storage and populates the healthRecords list.
     */
    fun fetchHealthRecordsFromFirestore() {
        healthRecords.clear()
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val storage = Firebase.storage
        val storageRef = storage.reference.child("users/$userId/pdfs")

        getAllRecords(storageRef, userId)
    }

    /**
     *  getAllRecords fetches all the health records stored by the senior citizen from Firebase Storage.
     *  It gets a reference to the Firebase Storage location where the health records are stored.
     *  It fetches the health records from Firebase Storage and populates the healthRecords list.
     *  @param storageRef the reference to the Firebase Storage location where the health records are stored
     *  @param userId the user ID of the senior citizen
     *  @return a list of health records stored by the senior citizen
     *  @throws Exception if there is an error fetching the health records from Firebase Storage
     */
    fun getAllRecords(storageRef: StorageReference, userId: String) {
        healthRecords.clear()
        storageRef.listAll()
            .addOnSuccessListener { result ->
                for (fileRef in result.items) {
                    val fileName = fileRef.name
                    val fileUrl = "users/$userId/pdfs/" + fileName
                    val healthRecord = HealthRecord(fileName, fileUrl)
                    healthRecords.add(healthRecord)
                }
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.e(
                    "HealthRecordsActivity",
                    "Error fetching health records from storage",
                    exception
                )
                Toast.makeText(
                    this,
                    "Error fetching health records from storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * pickPdf allows the senior citizen to pick a PDF file from their device.
     * It creates an intent to pick PDF files.
     * It starts the activity to pick a PDF file.
     */
    private fun pickPdf() {
        // Create an intent to pick PDF files
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        // Start the activity to pick a PDF file
        pickPdfFile.launch(intent)
    }
}