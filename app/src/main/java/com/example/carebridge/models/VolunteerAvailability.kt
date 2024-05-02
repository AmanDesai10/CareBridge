package com.example.carebridge.models

data class VolunteerAvailability(
    val userId: String,
    val date: String,
    val slots: List<String>,
    val bookedBy: String
) {
    constructor(): this("", "", emptyList(), "")
}
