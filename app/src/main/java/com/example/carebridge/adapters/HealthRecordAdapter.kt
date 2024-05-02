package com.example.carebridge.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.models.HealthRecord
import com.example.carebridge.utils.openPdfFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

/**
 * HealthRecordAdapter is a RecyclerView adapter that displays a list of health records.
 * @param records the list of health records to display
 * @param context the context of the activity or fragment
 */
class HealthRecordAdapter(
    private val records: ArrayList<HealthRecord>,
    private val context: Context
) :
    RecyclerView.Adapter<HealthRecordAdapter.ViewHolder>() {

    /**
     * onCreateViewHolder creates a new ViewHolder for the RecyclerView.
     * @param parent the parent ViewGroup
     * @param viewType the view type
     * @return a new ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_record, parent, false)
        return ViewHolder(view)
    }

    /**
     * onBindViewHolder binds the data to the ViewHolder at the given position.
     * @param holder the ViewHolder to bind the data to
     * @param position the position of the item in the list
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get a reference to Firebase Storage
        val storage = Firebase.storage
        val record = records[position]

        // Get a reference to the record in Firebase Storage
        val storageRef = storage.reference.child(record.fileUrl)

        // Bind the data to the ViewHolder
        holder.fileNameTextView.text = record.fileName

        // Set an onClickListener to delete the record
        holder.deleteButton.setOnClickListener {
            deleteHealthRecord(position)
        }

        // Set an onClickListener to open the PDF file
        holder.cardView.setOnClickListener {
            storageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    openPdfFile(context, uri.toString())

                    Log.d("HealthRecordAdapter", "Download URL: $uri")
                }
                .addOnFailureListener { exception ->
                    Log.e("HealthRecordAdapter", "Error getting download URL", exception)
                }
        }
    }

    /**
     * getItemCount returns the number of items in the list.
     * @return the number of items in the list
     */
    override fun getItemCount(): Int {
        return records.size
    }

    /**
     * ViewHolder is a class that represents a single item in the RecyclerView.
     * @param itemView the view of the item
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteIconImageView)
        val cardView: CardView = itemView.findViewById(R.id.pdfHealthRecordCardView)
    }

    /**
     * deleteHealthRecord deletes a health record from Firebase Storage and the local list.
     * @param position the position of the record in the list
     */
    private fun deleteHealthRecord(position: Int) {
        val recordToDelete = records[position]
        val fileName = recordToDelete.fileName

        // Get a reference to the record in Firebase Storage
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user is logged in before deleting the record
        // Show a toast message if the user is not logged in
        // and return from the function
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = Firebase.storage.reference.child("users/$userId/pdfs/$fileName")

        // Delete the record from Firebase Storage
        storageRef.delete()
            .addOnSuccessListener {
                // Record deleted successfully from Firebase Storage

                // Now remove the record from the local list
                records.removeAt(position)

                // Notify the adapter of the item removal
                notifyItemRemoved(position)

                // Show a toast message
                Toast.makeText(context, "Deleted: $fileName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Failed to delete the record from Firebase Storage
                Log.e("HealthRecordAdapter", "Error deleting record", exception)
                Toast.makeText(context, "Error deleting record", Toast.LENGTH_SHORT).show()
            }
    }
}
