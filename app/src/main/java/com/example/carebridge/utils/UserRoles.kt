package com.example.carebridge.utils

enum class UserRoles {
    FAMILY_FRIENDS,
    SENIOR_CITIZEN,
    VOLUNTEER;

    fun getDescription(): String {
        return when (this) {
            FAMILY_FRIENDS -> "Family and Friends"
            SENIOR_CITIZEN -> "Senior Citizen"
            VOLUNTEER -> "Volunteer"
        }
    }

    companion object {
        fun getRoles(): List<String> {
            return UserRoles.entries.map { it.getDescription() }
        }
    }
}
