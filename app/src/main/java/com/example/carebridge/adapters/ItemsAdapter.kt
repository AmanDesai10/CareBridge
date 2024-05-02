package com.example.carebridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.models.Item

class ItemsAdapter(private val itemsList: MutableList<String>, private val listener: ItemClickListener) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    interface ItemClickListener {
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val editImageView: ImageView = itemView.findViewById(R.id.editItem)
        val deleteImageView: ImageView = itemView.findViewById(R.id.deleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]
        holder.itemNameTextView.text = item

        // Set click listeners for edit and delete actions
        holder.editImageView.setOnClickListener {
            listener.onEditClick(position)
        }
        holder.deleteImageView.setOnClickListener {
            listener.onDeleteClick(position)
        }
    }
    fun updateItems(newItems: List<String>) {
        itemsList.clear()
        itemsList.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
}
