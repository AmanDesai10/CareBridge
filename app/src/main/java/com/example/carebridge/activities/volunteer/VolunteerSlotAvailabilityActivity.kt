package com.example.carebridge.activities.volunteer

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carebridge.R
import com.example.carebridge.models.VolunteerAvailability
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for managing volunteer's slot availability.
 */
class VolunteerSlotAvailabilityActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var firestore: FirebaseFirestore
    private val timeSlotTextViews: List<TextView> by lazy {
        listOf(
            findViewById(R.id.timeSlotTextView1),
            findViewById(R.id.timeSlotTextView2),
            findViewById(R.id.timeSlotTextView3),
            findViewById(R.id.timeSlotTextView4),
            findViewById(R.id.timeSlotTextView5),
            findViewById(R.id.timeSlotTextView6),
            findViewById(R.id.timeSlotTextView7),
            findViewById(R.id.timeSlotTextView8),
            findViewById(R.id.timeSlotTextView9)
        )
    }
    private val selectedSlots = mutableListOf<String>()
    private lateinit var sharedPreferncesManager: SharedPreferncesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_slot_availability)

        firestore = FirebaseFirestore.getInstance()
        sharedPreferncesManager = SharedPreferncesManager((this))
        val userId = sharedPreferncesManager.getValue("id")

        calendarView = findViewById(R.id.calendarView)

        // Get the current date initially
        val currentDate = Calendar.getInstance()
        val currentFormattedDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.time)

        // Set initial availability for the current date
        retrieveAvailability(userId.toString(), currentFormattedDate)
        var formattedSelectedDate: String = currentFormattedDate
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formattedSelectedDate = sdf.format(selectedDate.time)
            selectedSlots.clear() // Clear previously selected slots when changing the date
            // Retrieve availability for the selected date from Firestore
            retrieveAvailability(userId.toString(), formattedSelectedDate)
        }

        // Set click listener for time slots
        timeSlotTextViews.forEach { textView ->
            textView.setOnClickListener {
                val selectedTime = textView.text.toString()
                if (selectedSlots.contains(selectedTime)) {
                    // Slot already selected, remove it
                    selectedSlots.remove(selectedTime)
                } else {
                    // Slot not selected, add it
                    selectedSlots.add(selectedTime)
                }
                // Toggle selection state and update background color
                textView.isSelected = !textView.isSelected
            }
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val volunteerAvailability =
                VolunteerAvailability(userId.toString(), formattedSelectedDate, selectedSlots, "")
            storeAvailability(volunteerAvailability)
        }
    }

    /**
     * Retrieves availability data for the selected date from Firestore.
     * Clears previously selected slots, processes retrieved documents to get available slots, and updates UI to reflect availability.
     *
     * @param userId The ID of the volunteer user.
     * @param date The selected date for retrieving availability.
     */
    private fun retrieveAvailability(userId: String, date: String) {
        // Retrieve availability data for the selected date from Firestore
        firestore.collection("volunteer_availability")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                // Clear previously selected slots
                selectedSlots.clear()
                // Process retrieved documents to get available slots
                for (document in documents) {
                    val slots = document["slots"] as? List<String>
                    if (slots != null) {
                        selectedSlots.addAll(slots)
                    }
                }
                // Update UI to reflect availability
                updateAvailabilityUI()
            }
            .addOnFailureListener { e ->
                // Handle failures
                println("Error retrieving availability: $e")
            }
    }


    /**
     * Stores volunteer availability in Firestore.
     * Checks if a document already exists for the user and date combination, creates a new one if not found, otherwise updates the existing one.
     *
     * @param availability The VolunteerAvailability object representing the availability data to be stored.
     */
    private fun storeAvailability(availability: VolunteerAvailability) {
        // Check if a document already exists for the user and date combination
        firestore.collection("volunteer_availability")
            .whereEqualTo("userId", availability.userId)
            .whereEqualTo("date", availability.date)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No document found, create a new one
                    createNewDocument(availability)
                } else {
                    // Document found, update the existing one
                    val docId = documents.documents[0].id
                    updateDocument(docId, availability)
                }
            }
            .addOnFailureListener { e ->
                // Handle failures
                println("Error checking availability document: $e")
            }
    }

    /**
     * Creates a new document for volunteer availability in Firestore.
     *
     * @param availability The VolunteerAvailability object representing the availability data to be stored.
     */
    private fun createNewDocument(availability: VolunteerAvailability) {
        // Store the availability in Firestore by adding a new document
        firestore.collection("volunteer_availability")
            .add(availability)
            .addOnSuccessListener { documentReference ->
                // Handle success
                println("Availability stored with ID: ${documentReference.id}")
                Toast.makeText(this, "Availability added successfully!", Toast.LENGTH_LONG)
            }
            .addOnFailureListener { e ->
                // Handle failures
                println("Error storing availability: $e")
            }
    }

    /**
     * Updates an existing document for volunteer availability in Firestore.
     *
     * @param docId The ID of the document to be updated.
     * @param availability The VolunteerAvailability object representing the updated availability data.
     */
    private fun updateDocument(docId: String, availability: VolunteerAvailability) {
        // Update the availability document in Firestore
        firestore.collection("volunteer_availability")
            .document(docId)
            .set(availability)
            .addOnSuccessListener {
                // Handle success
                println("Availability document updated: $docId")
                Toast.makeText(this, "Availability updated successfully!", Toast.LENGTH_LONG)
            }
            .addOnFailureListener { e ->
                // Handle failures
                println("Error updating availability document: $e")
            }
    }

    /**
     * Updates the UI to reflect availability by changing the background colors of time slots.
     * Iterates through time slots TextViews and sets their isSelected property based on availability.
     */
    private fun updateAvailabilityUI() {
        // Update UI to reflect availability by changing background colors of time slots
        timeSlotTextViews.forEach { textView ->
            val slotText =
                textView.text.toString()// Slot is not available, set background color to light
            // Slot is available, set background color to darker
            textView.isSelected = selectedSlots.contains(slotText)
        }
    }
}