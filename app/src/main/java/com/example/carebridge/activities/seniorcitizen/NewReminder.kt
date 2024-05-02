package com.example.carebridge.activities.seniorcitizen

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carebridge.R
import com.example.carebridge.models.Reminder
import com.example.carebridge.utils.ReminderReceiver
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Activity to create a new reminder or edit an existing one.
 */
class NewReminder : AppCompatActivity() {
    private lateinit var sharedPreferncesManager: SharedPreferncesManager
    private val firestore = FirebaseFirestore.getInstance()
    private val remindersCollection = firestore.collection("reminders")
    private val REQUEST_ALARM_PERMISSION = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)
        var selectedDate: String = ""
        val editTextReminderName = findViewById<EditText>(R.id.editTextReminderName)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        sharedPreferncesManager = SharedPreferncesManager(this)

        // Check if intent contains reminder details for editing
        val reminderId = intent.getStringExtra("reminderId")
        if (reminderId != null) {
            // If reminderId is not null, it means we're editing an existing reminder
            val reminderName = intent.getStringExtra("reminderName")
            val selectedTime = intent.getStringExtra("selectedTime")
            val selectedDate = intent.getStringExtra("selectedDate")

            // Set reminder details to corresponding UI elements for editing
            editTextReminderName.setText(reminderName)
            // Set TimePicker's hour and minute based on selectedTime
            val timeParts = selectedTime?.split(":")
            if (timeParts?.size == 2) {
                timePicker.hour = timeParts[0].toInt()
                timePicker.minute = timeParts[1].toInt()
            }
            // Set CalendarView's date based on selectedDate
            val calendar = Calendar.getInstance()
            val dateParts = selectedDate?.split("-")
            if (dateParts?.size == 3) {
                // Extract year, month, and day parts
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt() - 1 // Calendar.MONTH is zero-based
                val dayOfMonth = dateParts[2].toInt()

                // Set the calendar with the extracted date parts
                calendar.set(year, month, dayOfMonth)

                // Set the CalendarView date
                calendarView.date = calendar.timeInMillis
            }
        }
        val calendar = Calendar.getInstance()

        // Set default value for selected date
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        // Listener for the submit button
        buttonSubmit.setOnClickListener {
            // Get the text from the EditText for reminder name
            val reminderName = editTextReminderName.text.toString()

            if (reminderName.isEmpty()) {
                // Show a toast to the user to add text
                Toast.makeText(this, "Please enter a reminder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the selected time from TimePicker
            val hour = timePicker.hour
            val minute = timePicker.minute
            val selectedTime = String.format("%02d:%02d", hour, minute)

            // Create a new reminder object
            val userId = sharedPreferncesManager.getValue("id") ?: ""
            val reminder = Reminder(userId, reminderId ?: UUID.randomUUID().toString(), reminderName, selectedTime, selectedDate)

            // Add or update the reminder in Firestore
            addOrUpdateReminderToFirebase(reminder)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            scheduleNotification(calendar, reminder.reminderName)
        }
    }


    private fun addOrUpdateReminderToFirebase(reminder: Reminder) {
        // Determine whether to add a new reminder or update an existing one
        val operation = if (reminder.reminderId.isNotBlank()) {
            remindersCollection.document(reminder.reminderId).set(reminder)
        } else {
            remindersCollection.add(reminder)
        }

        // Perform the add/update operation
        operation
            .addOnSuccessListener {
                val message = if (reminder.reminderId.isNotBlank()) "Reminder updated successfully" else "Reminder added successfully"

                // Show a toast indicating success
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Redirect to UserReminder activity
                val intent = Intent(this, UserReminder::class.java)
                startActivity(intent)
                finish() // Finish the current activity
            }
            .addOnFailureListener { e ->
                val message = if (reminder.reminderId.isNotBlank()) "Failed to update reminder" else "Failed to add reminder"

                // Show a toast indicating failure
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }
    private fun scheduleNotification(calendar: Calendar, message: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("message", message)
            putExtra("title", "Reminder")
        }
        val requestCode = UUID.randomUUID().mostSignificantBits.toInt()
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
