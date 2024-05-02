package com.example.carebridge.models

/**
 * HealthRecord is a data class that represents a health record of a senior citizen.
 * @param fileName the name of the health record file
 * @param fileUrl the URL of the health record file
 */
data class HealthRecord(
    val fileName: String,
    val fileUrl: String
)
