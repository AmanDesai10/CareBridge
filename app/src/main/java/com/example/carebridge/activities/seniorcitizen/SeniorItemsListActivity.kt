package com.example.carebridge.activities.seniorcitizen

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.ItemsAdapter
import com.example.carebridge.R
import com.example.carebridge.utils.AuthManager
import com.example.carebridge.utils.FCMNotificationSender
import com.example.carebridge.utils.SharedPreferncesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * This activity helps to show the list of items and enables to edit or delete the item.
 * */

class SeniorItemsListActivity : AppCompatActivity(), ItemsAdapter.ItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemsList: MutableList<String>
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferncesManager: SharedPreferncesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senior_items_list)

        db = Firebase.firestore
        itemsList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerView)


        // Set up the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemsAdapter(itemsList, this)

        // Handle the add button click
        findViewById<Button>(R.id.addButton).setOnClickListener {
            showAddItemDialog()
        }


//        db.collection("items")
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    itemsList.add(document.getString("name") ?: "")
//                }
//                (recyclerView.adapter as ItemsAdapter).notifyDataSetChanged()
//            }
        sharedPreferncesManager = SharedPreferncesManager(this)
        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.getString("userId")
                    if (userId != null && userId == sharedPreferncesManager.getValue("id")) {
                        itemsList.add(document.getString("name") ?: "")
                    }
                }
                (recyclerView.adapter as ItemsAdapter).notifyDataSetChanged()
            }
    }

    /**
     * This function shows dialog to add item
     *
     */
    private fun showAddItemDialog() {
        sharedPreferncesManager = SharedPreferncesManager(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.submitButton).setOnClickListener {
            val itemName = dialogView.findViewById<EditText>(R.id.itemNameEditText).text.toString()
            if (itemName.isNotEmpty()) {
                // Add item to Firestore
                val item = hashMapOf(
                    "name" to itemName,
                    "userId" to sharedPreferncesManager.getValue("id")
                )
                db.collection("items").add(item)
                    .addOnSuccessListener { documentReference ->
                        itemsList.add(itemName)
                        (recyclerView.adapter as ItemsAdapter).notifyDataSetChanged()
                        alertDialog.dismiss()
                        val title: String =
                            sharedPreferncesManager.getValue("name") + " has added item."
                        val message: String = itemName + " has been added."

                        val fcmNotificationSender = FCMNotificationSender()
                        val authManager = AuthManager(this)

                        authManager.fetchCombinedFCMTokens()
                            .addOnSuccessListener { familyMemberFCMList ->
                                for (fcm in familyMemberFCMList) {
                                    fcmNotificationSender.sendNotification(fcm, title, message)
                                }
                            }
                    }
            }
        }
        alertDialog.show()
    }

    /**
     * This function gives edit option */
    override fun onEditClick(position: Int) {
// Get the current item name
        val currentItemName = itemsList[position]

        // Show a dialog to edit the item name
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<EditText>(R.id.itemNameEditText).setText(currentItemName)
        dialogView.findViewById<Button>(R.id.submitButton).setOnClickListener {
            val newItemName =
                dialogView.findViewById<EditText>(R.id.itemNameEditText).text.toString()
            if (newItemName.isNotEmpty() && newItemName != currentItemName) {
                // Update the item in Firestore
                db.collection("items")
                    .whereEqualTo("name", currentItemName)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                document.reference.update("name", newItemName)
                            }
                            // Update the local list and notify the adapter
                            itemsList[position] = newItemName
                            (recyclerView.adapter as ItemsAdapter).notifyItemChanged(position)
                        }
                    }
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    override fun onDeleteClick(position: Int) {
        val itemName = itemsList[position]

        // Query Firestore to find the document(s) with the matching item name
        db.collection("items")
            .whereEqualTo("name", itemName)
            .get()
            .addOnSuccessListener { documents ->
                // If any documents are found, delete them
                if (!documents.isEmpty) {
                    for (document in documents) {
                        document.reference.delete()
                    }
                    // Remove the item from the local list and notify the adapter
                    itemsList.removeAt(position)
                    (recyclerView.adapter as ItemsAdapter).notifyItemRemoved(position)
                }
            }
    }
}

