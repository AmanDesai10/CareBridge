package com.example.carebridge.activities.seniorcitizen

import VolunteersAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.models.Volunteer
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * This activity allows seniors to book volunteers based on date and time.
 *
 * */

class SeniorBookVolunteerActivity : AppCompatActivity() {

    private lateinit var calendarIcon: ImageView
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var selectedDateTextView: TextView
    private lateinit var volunteerRecyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var volunteersAdapter: VolunteersAdapter
    private lateinit var sharedPrefsManager: SharedPreferncesManager

    //    variable to set a date selected for booking a volunteer
    private var dateSelected: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senior_book_volunteer)
        timePeriodSpinner =
            findViewById(R.id.timePeriodSpinner) // Time period to book the volunteer

        calendarIcon = findViewById(R.id.calendarIcon) // calendar pop-up for selecting the date
        selectedDateTextView = findViewById(R.id.selectedDateTextView)
        volunteerRecyclerView = findViewById(R.id.seniorvolunteerRecyclerView)
        sharedPrefsManager =
            SharedPreferncesManager(this) // storing the using in memory - using shared preference

//        set the time period with loop to set all the time periods.
//        time periods are in intervals of hours.
        val timePeriods = mutableListOf<String>()
        for (hour in 10..19) {
            val startTime = String.format(Locale.getDefault(), "%02d:00", hour)
            val endTime = String.format(Locale.getDefault(), "%02d:00", hour + 1)
            timePeriods.add("$startTime-$endTime")
        }
        val newAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods)
        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = newAdapter

//        connect with Database.
        db = FirebaseFirestore.getInstance()

//        show date picker dialog.
        selectedDateTextView.setOnClickListener {
            showDatePickerDialog()
        }
//        show calendar dialog to set the date.
        calendarIcon.setOnClickListener {
            showDatePickerDialog()
        }

        volunteersAdapter = VolunteersAdapter(emptyList()) { volunteer ->
            showBookVolunteerDialog(volunteer)
        }

        volunteerRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SeniorBookVolunteerActivity)
            adapter = volunteersAdapter
        }
    }

    /**
     * This function shows date picker dialog to pick the date from calendar.
     * */

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
//            format the date for maintaining uniformity with the database and other activities in the project.
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)
                dateSelected = formattedDate //format the date.
                selectedDateTextView.text =
                    "Selected Date: $formattedDate" // show the date in UI, so that user can see their preferred date.

                // Query Firestore for available volunteers on the selected date
                queryAvailableVolunteers(
                    formattedDate,
                    volunteersAdapter
                ) // Pass volunteersAdapter here
            }, year, month, day)

        datePickerDialog.show()
    }

    /**
     * Below function calls the database to get all the available volunteers.
     * @param: date - to pass the date of availability
     * @param: volunteersAdapter - to get the model of volunteer */
    private fun queryAvailableVolunteers(date: String, volunteersAdapter: VolunteersAdapter) {

//        check the Database collection of volunteer's availibility.
//        Check on the selected date, if the volunteer is available, then get it's user id
        db.collection("volunteer_availability")
            .whereEqualTo("date", date)
            .whereEqualTo("bookedBy", "")
            .get()
            .addOnSuccessListener { documents ->
                val availableVolunteers = mutableListOf<Volunteer>()

                for (document in documents) {
                    val userId = document.getString("userId")
//                    get the userId of the user and pass it to "Users" table to get all the details of that user.
                    userId?.let { id ->
                        lifecycleScope.launch {
                            val volunteer =
                                getVolunteerDetailsById(id) // call the getVolunteerDetailsById function on a given id

                            volunteer?.let {
                                availableVolunteers.add(it) // if volunteers are available, show the list of volunteers on that particular date.
                                volunteersAdapter.updateVolunteers(availableVolunteers) // update the volunteers data.
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SeniorBookVolunteer", "Error querying available volunteers: $e")
            }
    }

    /**
     * Based on the date and the slots, the details of volunteer is possed to users collection
     * @param: id - to be fetched from users table
     * */
    private suspend fun getVolunteerDetailsById(id: String): Volunteer? {
        return withContext(Dispatchers.IO) {
            try {

//                call the "Users" table from given id, to take the user details of the volunteer.
//                if user details exists, fetch details.
                val document = db.collection("users").document(id).get().await()
                if (document.exists()) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("username") ?: ""
                    val age = document.getString("age") ?: ""
                    val contact = document.getString("phoneNumber") ?: ""
                    return@withContext Volunteer(id, name, age, contact)
                } else {
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("SeniorBookVolunteer", "Error retrieving volunteer details: $e")
                return@withContext null
            }
        }
    }

    /**    Dialog box to show the details of the selected volunteer and confirm the booking */
    private fun showBookVolunteerDialog(volunteer: Volunteer) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_book_volunteer, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Book Volunteer")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create().apply {
            show()
        }

        dialogView.findViewById<TextView>(R.id.volunteerNameTextView).text =
            "Name: " + volunteer.name
        dialogView.findViewById<TextView>(R.id.volunteerAgeTextView).text = "Age: " + volunteer.age
        dialogView.findViewById<TextView>(R.id.volunteerContactTextView).text =
            "Contact: " + volunteer.contact

        dialogView.findViewById<Button>(R.id.bookVolunteerButton).setOnClickListener {
            val sharedId = sharedPrefsManager.getValue("id")
            if (sharedId != null) {
                updateVolunteerAvailability(volunteer.id.toString(), dateSelected, sharedId)
                dialog.dismiss()
            }
        }
    }

    /**    If volunteer is booked by the senior citizen, update the database with the id of the senior citizen
     *    So, that volunteer can get a list of bookings and the details of senior citizens. */
    private fun updateVolunteerAvailability(userId: String, date: String, bookedBy: String) {
        // Get the reference to the document to be updated
        val docRef = db.collection("volunteer_availability").whereEqualTo("userId", userId)
            .whereEqualTo("date", date)

        // Update the document with the new bookedBy value
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("volunteer_availability").document(document.id)
                        .update("bookedBy", bookedBy)
                        .addOnSuccessListener {
                            Log.e("SeniorBookVolunteer", "Document updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("SeniorBookVolunteer", "Error updating document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SeniorBookVolunteer", "Error getting documents: $e")
            }
    }
}
