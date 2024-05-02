package com.example.carebridge.models

/**
 * Data class representing the modal for the ClientBookingConfirmation
 */
data class ClientBookingConfirmation(
    val bookingDate: String,
    val clientName: String,
    val clientDescription: String,
    val clientPhoneNumber: String,
    val clientEmail: String
)