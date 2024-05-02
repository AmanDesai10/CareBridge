package com.example.carebridge.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.models.ClientBookingConfirmation

/**
 * Adapter class performing functionality of inflating the card layout to the ClientConfirmation.
 */
class ClientConfirmationAdapter(val clientConfirmationList: ArrayList<ClientBookingConfirmation>) :
    RecyclerView.Adapter<ClientConfirmationAdapter.MyViewHolder>() {

    /**
     * Viewholder class for holding views in the Recyclerview representing each card
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Fetching each view from the Layout
         */
        val bookingDate: TextView = itemView.findViewById(R.id.bookingDate)
        val clientName: TextView = itemView.findViewById(R.id.clientName)
        val clientDescription: TextView = itemView.findViewById(R.id.description)
        val clientConfirmationCard: View = itemView.findViewById((R.id.clientConfirmationItem))

        /**
         * Function binding the data to the ClientBookingConfirmation
         */
        fun bind(clientBookingConfirmation: ClientBookingConfirmation) {
            bookingDate.text = "Booking Date: " + clientBookingConfirmation.bookingDate
            clientName.text = "Client Name: " + clientBookingConfirmation.clientName
            clientDescription.text = "Address: " + clientBookingConfirmation.clientDescription
        }
    }

    /**
     * Creating view holder instance
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClientConfirmationAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_confirmation_item, parent, false)
        return MyViewHolder(view)
    }

    /**
     * Returning the number of items in the list
     */
    override fun getItemCount(): Int {
        return clientConfirmationList.size
    }

    /**
     * Binding data to each item in the RecyclerView
     */
    override fun onBindViewHolder(holder: ClientConfirmationAdapter.MyViewHolder, position: Int) {
        holder.bind(clientConfirmationList[position])


        /**
         * Set OnClickListener for the card view in each item
         */
        holder.clientConfirmationCard.setOnClickListener {
            val builder = AlertDialog.Builder(holder.clientConfirmationCard.context)

            /**
             * Inflating custom layout for the AlertDialog
             */
            val popUpLayout = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.client_confirmation_individual_card, null)
            builder.setView(popUpLayout)

            /**
             * Views inside the custom layout
             */
            val popUpClientName = popUpLayout.findViewById<TextView>(R.id.popUpClientName)
            val popUpClientBookingDate = popUpLayout.findViewById<TextView>(R.id.popUpBookingDate)
            val popUpClientDescription =
                popUpLayout.findViewById<TextView>(R.id.popUpClientDescription)
            val popUpClientEmail = popUpLayout.findViewById<TextView>(R.id.popUpClientEmail)
            val popUpClientPhoneNumber =
                popUpLayout.findViewById<TextView>(R.id.popUpClientPhoneNumber)

            /**
             * Set text for the views in the AlertDialog
             */
            //
            popUpClientName.text = "Client Name: " + clientConfirmationList[position].clientName
            popUpClientBookingDate.text =
                "Booking Date: " + clientConfirmationList[position].bookingDate
            popUpClientDescription.text =
                "Address: " + clientConfirmationList[position].clientDescription
            popUpClientEmail.text = "Email: " + clientConfirmationList[position].clientEmail
            popUpClientPhoneNumber.text =
                "Contact Number: " + clientConfirmationList[position].clientPhoneNumber

            /**
             * Set title and button for the AlertDialog
             */
            builder.setTitle("Confirmation")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    /**
     * Function to update the list with new data
     */
    fun updateList(newList: List<ClientBookingConfirmation>) {
        clientConfirmationList.clear()
        clientConfirmationList.addAll(newList)
        notifyDataSetChanged()
    }
}




