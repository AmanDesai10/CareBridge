package com.example.carebridge.models

data class Reminder(val userId: String, val reminderId: String, val reminderName: String, val selectedTime: String, val selectedDate: String) {
    constructor() : this("", "", "", "", "")
}