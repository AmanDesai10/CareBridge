package com.example.carebridge.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.carebridge.models.User
import com.example.carebridge.services.falldetection.FallDetectionService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class AuthManager(private val context: Context) {
    private var auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val sharedPreferncesManager = SharedPreferncesManager(context)

    private fun stopFallDetectionService() {
        val intent = Intent(context, FallDetectionService::class.java)
        context.stopService(intent)
    }

    fun registerUser(
        name: String,
        email: String,
        username: String,
        phoneNumber: String,
        age: String,
        gender: String,
        address: String,
        password: String,
        role: String,
        familyMembers: ArrayList<String>,
        callback: (String) -> Unit
    ) {
        if (!validateUserInfo(email, password, username, callback)) {
            return
        }
        Log.d("username", username)

        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                val signInMethods = result.signInMethods
                if (signInMethods.isNullOrEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            val uid = user?.uid
                            if (uid != null) {
                                val newUser = User(
                                    uid,
                                    name,
                                    email,
                                    username,
                                    phoneNumber,
                                    age,
                                    gender,
                                    address,
                                    role,
                                    familyMembers,
                                    ""
                                )
                                addUser(newUser) { addUserResult ->
                                    if (addUserResult == "Success") {
                                        callback("Success")
                                    } else {
                                        // There was an issue adding user to Firestore
                                        // Rollback Firebase Authentication user creation
                                        user.delete()
                                            .addOnCompleteListener {
                                                callback("Error while creating account... Please try again later.")
                                            }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.v("error2", e.toString())
                            callback("Error while creating account... Please try again later.")
                        }
                } else {
                    // Email is already registered
                    callback("This email address is already registered.")
                }
            }
            .addOnFailureListener { e ->
                // Error occurred while checking email registration
                Log.v("error", e.toString())
                callback("Error while creating account... Please try again later.")
            }
    }


    fun signIn(email: String, password: String, role: String, callback: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val uid = user?.uid
                if (uid != null) {
                    getUserById(uid) { user ->
                        if (user == null) {
                            callback("Error while login... Please try again later.")
                        } else {
                            if (role != user.role) {
                                callback("Invalid email/password/role.")
                            } else {
                                saveUserDetails(
                                    user.id,
                                    user.name,
                                    user.email,
                                    user.username,
                                    user.phoneNumber,
                                    user.age,
                                    user.gender,
                                    user.address,
                                    role,
                                )
                                callback("Success")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("signIn", "signInWithEmailAndPassword failed", exception)
                callback("Invalid email or password.")
            }
    }


    fun logout() {

        auth.signOut()
        sharedPreferncesManager.removeValue("username")
        sharedPreferncesManager.removeValue("role")
        sharedPreferncesManager.removeValue("id")
        sharedPreferncesManager.removeValue("email")
        FirebaseMessaging.getInstance().deleteToken();

    }

    private fun addUser(user: User, callback: (String) -> Unit) {
        usersCollection.document(user.id)
            .set(user)
            .addOnSuccessListener {
                callback("Success")
            }
            .addOnFailureListener { e ->
                Log.v("error1", e.toString())
                callback("Error while creating account... Please try again later.")
            }
    }

    private fun getUserById(userId: String, callback: (User?) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    //    private fun saveUserDetails(username: String, email: String, role: String, id: String) {
    private fun saveUserDetails(
        id: String,
        name: String,
        email: String,
        username: String,
        phoneNumber: String,
        age: String,
        gender: String,
        address: String,
        role: String,
    ) {
        sharedPreferncesManager.saveKey("id", id)
        sharedPreferncesManager.saveKey("name", name)
        sharedPreferncesManager.saveKey("email", email)
        sharedPreferncesManager.saveKey("username", username)
        sharedPreferncesManager.saveKey("phoneNumber", phoneNumber)
        sharedPreferncesManager.saveKey("age", age)
        sharedPreferncesManager.saveKey("gender", gender)
        sharedPreferncesManager.saveKey("address", address)
        sharedPreferncesManager.saveKey("role", role)
    }

    private fun validateUserInfo(
        email: String,
        password: String,
        username: String,
        callback: (String) -> Unit
    ): Boolean {
        Log.d("username", username)
        if (!isValidUsername(username)) {
            callback("Username can contains Alphanumeric and minimum length should be 3.")
            return false
        }

        if (!isValidEmail(email)) {
            callback("Invalid email.")
            return false
        }

        if (!isValidPassword(password)) {
            callback("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailRegex.matches(email)
    }

    private fun isValidUsername(username: String): Boolean {
        val usernameRegex = Regex("^[a-zA-Z0-9_-]{3,15}$")
        return usernameRegex.matches(username)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex =
            Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\$@!%*?&])[A-Za-z\\d\$@!%*?&]{8,}\$")
        return passwordRegex.matches(password)
    }

    private fun currentUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance().collection("users")
            .document(currentUserId().toString())
    }

    fun setFCMToke() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Token retrieval successful, you can now use 'token' as needed
                // For example, you can log it or send it to your server
                Log.d("token is: ", token)
                this.currentUserDetails().update("fcmToken", token)
            } else {
                // Token retrieval failed
                println("Failed to retrieve FCM token: ${task.exception}")
            }
        }
    }

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferncesManager.getValue("id") != null
    }

    fun getUserRole(): String? {
        return sharedPreferncesManager.getValue("role")
    }

    fun fetchCombinedFCMTokens(): Task<List<String>> {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // Get the current user's ID
        val currentUserId = auth.currentUser?.uid

        // Fetch the current user document
        val userCollection = db.collection("users")
        val currentUserDocRef = currentUserId?.let { userCollection.document(it) }

        return if (currentUserDocRef != null) {
            currentUserDocRef.get().continueWithTask { task ->
                val currentUserData = task.result.toObject(User::class.java)
                    ?: throw IllegalStateException("Current user data not found")

                val familyMemberIds = currentUserData.familyMembers

                // Fetch FCM tokens for family members
                fetchFCMTokensForUsers(db, familyMemberIds)
            }
        } else {
            val exceptionTask = TaskCompletionSource<List<String>>()
            exceptionTask.setException(IllegalStateException("Current user ID not found"))
            exceptionTask.task
        }
    }

    private fun fetchFCMTokensForUsers(
        db: FirebaseFirestore,
        userIds: List<String>
    ): Task<List<String>> {
        val userCollection = db.collection("users")
        val tasks = mutableListOf<Task<String>>()

        // Fetch FCM token for each user
        for (userId in userIds) {
            Log.d("userid: ", userId)
            val task = userCollection.document(userId).get()
                .continueWith { task ->
                    val user = task.result.toObject(User::class.java)
                        ?: throw IllegalStateException("User with ID $userId not found")
                    user.fcmToken
                }
            tasks.add(task)
        }

        // Wait for all tasks to complete
        return Tasks.whenAllSuccess(tasks)
    }

    fun fetchFCMTokenFromCurrentUser(callback: (String?) -> Unit) {
        val currentUserRef = currentUserDetails()

        currentUserRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val fcmToken = documentSnapshot.getString("fcmToken")
                    callback(fcmToken)
                } else {
                    callback(null) // User document does not exist
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCM Token", "Error fetching FCM Token", e)
                callback(null) // Error occurred while fetching FCM Token
            }
    }
}