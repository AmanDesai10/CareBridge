package com.example.carebridge.activities.seniorcitizen

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.carebridge.R
import com.example.carebridge.models.Reminder
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.example.carebridge.utils.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity to display and manage reminders for a user.
 */
class UserReminder : AppCompatActivity() {

    private lateinit var remindersListView: ListView
    private lateinit var remindersAdapter: ReminderAdapter
    private lateinit var remindersList: MutableList<Reminder>
    private lateinit var sharedPreferncesManager: SharedPreferncesManager
    private lateinit var userId: String
    private lateinit var addItemsButton: Button
    private val firestore = FirebaseFirestore.getInstance()
    private val remindersCollection = firestore.collection("reminders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_reminder)

        // Initialize ListView and its adapter
        remindersListView = findViewById(R.id.remindersListView)
        remindersList = mutableListOf()
        remindersAdapter = ReminderAdapter()
        remindersListView.adapter = remindersAdapter

        // Get user ID (assuming it's retrieved from SharedPreferences)
        sharedPreferncesManager = SharedPreferncesManager(this)
        userId = sharedPreferncesManager.getValue("id").toString()

        // Fetch reminders for the given user ID from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("reminders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reminder = document.toObject(Reminder::class.java)
                    remindersList.add(reminder)
                }
                remindersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle failures
            }

        // Initialize addItemsButton
        addItemsButton = findViewById(R.id.addItemsButton)

        // Set OnClickListener for addItemsButton
        addItemsButton.setOnClickListener {
            // Launch NewReminderActivity
            val intent = Intent(this, NewReminder::class.java)
            startActivity(intent)
        }
    }

    /**
     * Delete a reminder from Firestore and cancel its scheduled alarm.
     * @param reminderId The ID of the reminder to delete.
     */
    private fun deleteReminderFromFirestore(reminderId: String) {
        // Delete the reminder from Firestore collection using its ID
        remindersCollection.document(reminderId)
            .delete()
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }
    }

    /**
     * Custom ArrayAdapter for displaying reminders in the ListView.
     */
    inner class ReminderAdapter : ArrayAdapter<Reminder>(this, R.layout.list_item_reminder, remindersList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_reminder, parent, false)

            val reminderTextView: TextView = itemView.findViewById(R.id.reminderTextView)

            val reminder = getItem(position)
            reminder?.let {
                reminderTextView.text = "${it.reminderName}\n${it.selectedDate} ${it.selectedTime}"
            }

            val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
            deleteButton.setOnClickListener {
                // Get the reminder ID of the clicked item
                val reminder = remindersList[position]
                reminder?.let {
                    val reminderId = it.reminderId
                    // Remove the clicked item from the list
                    remindersList.removeAt(position)
                    // Notify the adapter that the data set has changed
                    notifyDataSetChanged()
                    // Delete the reminder from Firestore collection
                    deleteReminderFromFirestore(reminderId)
                    cancelScheduledReminder(reminder.selectedDate, reminder.selectedTime)
                }
            }

            val editButton : Button = itemView.findViewById((R.id.editButton))
            editButton.setOnClickListener {
                // Get the reminder at the clicked position
                val reminder = getItem(position)
                reminder?.let {
                    // Create an intent to open the NewReminder activity
                    val intent = Intent(context, NewReminder::class.java)
                    // Pass the reminder details as extras to the intent
                    intent.putExtra("reminderId", it.reminderId)
                    intent.putExtra("reminderName", it.reminderName)
                    intent.putExtra("selectedTime", it.selectedTime)
                    intent.putExtra("selectedDate", it.selectedDate)
                    // Start the NewReminder activity
                    context.startActivity(intent)
                }
            }

            return itemView
        }
    }

    /**
     * Cancel a scheduled reminder alarm.
     * @param selectedDate The date of the reminder.
     * @param selectedTime The time of the reminder.
     */
    private fun cancelScheduledReminder(selectedDate: String, selectedTime: String) {
        // Parse the selected date and time strings
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = dateFormatter.parse(selectedDate)
        val time = timeFormatter.parse(selectedTime)

        // Create Calendar instance and set it to the selected date
        val calendarDate = Calendar.getInstance().apply {
            timeInMillis = date.time
        }

        // Set the time components from selected time
        val calendarTime = Calendar.getInstance().apply {
            timeInMillis = time.time
        }

        // Set the time components from calendarTime to calendarDate
        calendarDate.apply {
            set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0) // Optional: Set seconds to 0
        }

        // Log the scheduled reminder time for debugging
        Log.d("Reminder", "Scheduled reminder time: ${calendarDate.time}")

        // Create the same intent as used for scheduling
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Get the AlarmManager service
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Log the cancellation time for debugging
        Log.d("Reminder", "Cancelling reminder at: ${System.currentTimeMillis()}")

        // Cancel the pending intent associated with the selected date and time
        alarmManager.cancel(pendingIntent)
    }
}
