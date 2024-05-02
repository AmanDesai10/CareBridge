package com.example.carebridge.adapters

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.activities.familyandfriends.FamilyFriendsView
import com.example.carebridge.models.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class SeniorCitizenAdapter(
    val seniorCitizenList: ArrayList<User>,
    familyMemberId: String,
    popupMenu: Boolean
) :
    RecyclerView.Adapter<SeniorCitizenAdapter.MyViewHolder>() {
    val popupMenu = popupMenu
    val familyMemberId = familyMemberId
    private var filteredList: ArrayList<User> = seniorCitizenList

    // ViewHolder class to hold the views for each city item
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val popUpCard: View = itemView.findViewById(R.id.seniorCitizenListCard)
        var nameTextView: TextView
        var emailTextView: TextView
        var contactNumberTextView: TextView

        // Initialize views and set onClickListener for item clicks
        init {
            nameTextView = itemView.findViewById(R.id.nameTv)
            emailTextView = itemView.findViewById(R.id.emailTv)
            contactNumberTextView = itemView.findViewById(R.id.contactNumberTv)
        }
    }

    // Inflate layout for city item and create ViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SeniorCitizenAdapter.MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item_family_member, parent, false)
        return MyViewHolder(v)
    }

    // Bind data to views for each city item
    override fun onBindViewHolder(holder: SeniorCitizenAdapter.MyViewHolder, position: Int) {
        holder.nameTextView.text = seniorCitizenList[position].name
        holder.emailTextView.text = seniorCitizenList[position].email
        holder.contactNumberTextView.text = seniorCitizenList[position].phoneNumber

        if (popupMenu) {
            holder.popUpCard.setOnClickListener {
                //popup menu
                val builder = AlertDialog.Builder(holder.itemView.context)

                val popUpLayout = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.popup_item_senior_citizen, null)
                builder.setView(popUpLayout)

                val popUpSeniorCitizenName: TextView =
                    popUpLayout.findViewById(R.id.popUpSeniorCitizenName)
                val linkSeniorCitizenButton: Button = popUpLayout.findViewById(R.id.linkButton)

                popUpSeniorCitizenName.text =
                    "Link to : " + seniorCitizenList[position].name
                linkSeniorCitizenButton.setOnClickListener {
                    addOrUpdateFamilyMember(seniorCitizenList[position].id, familyMemberId)
                    Toast.makeText(
                        holder.itemView.context,
                        "Senior Citizen Added",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(popUpLayout.context, FamilyFriendsView::class.java)
                    popUpLayout.context.startActivity(intent)
                }

                builder.setTitle("Add Senior Citizen")
                builder.create().show()
            }
        }
    }

    // Return the total number of city items
    override fun getItemCount(): Int {
        return seniorCitizenList.size
    }
}

private fun addOrUpdateFamilyMember(userID: String, newFamilyMember: String) {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    // Get reference to the user document
    val userRef: DocumentReference = firestore.collection("users").document(userID)

    // Fetch the user document
    userRef.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val document = task.result
            if (document != null && document.exists()) {
                // User document exists
                val familyMemberList = document.get("familyMembers") as? ArrayList<String>
                if (familyMemberList != null) {
                    // Family member list exists, update it
                    familyMemberList.add(newFamilyMember)
                    userRef.update("familyMembers", familyMemberList)
                } else {
                    // Family member list doesn't exist, create it
                    val newFamilyMemberList = arrayListOf(newFamilyMember)
                    userRef.set(
                        hashMapOf("familyMembers" to newFamilyMemberList),
                        SetOptions.merge()
                    )
                }
            } else {
                // User document doesn't exist
                Log.d("abc", "No such document")
            }
        } else {
            // Failed to fetch user document
            Log.d("abc", "get failed with ", task.exception)
        }
    }
}