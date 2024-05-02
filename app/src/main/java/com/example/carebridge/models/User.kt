package com.example.carebridge.models

data class User(
    val id: String,
    val name: String,
    val email: String,
    val username: String,
    val phoneNumber: String,
    val age: String,
    val gender: String,
    val address: String,
    val role: String,
    val familyMembers: ArrayList<String> = ArrayList(),
    val fcmToken: String
) {
    constructor() : this("", "", "", "", "", "", "", "", "", ArrayList(), "") // No-argument constructor

}
