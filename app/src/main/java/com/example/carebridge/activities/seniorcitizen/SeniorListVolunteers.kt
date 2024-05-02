package com.example.carebridge.activities.seniorcitizen

import VolunteersAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.models.Volunteer

class SeniorListVolunteers : AppCompatActivity() {

    private lateinit var volunteerRecyclerView: RecyclerView
    private lateinit var volunteersList: MutableList<Volunteer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senior_list_volunteers)

        volunteerRecyclerView = findViewById(R.id.volunteersListRecycleView)
        volunteersList = mutableListOf()


        val onItemClick: (Volunteer) -> Unit = {}

        volunteersList.add(
            Volunteer(
                name = "John Doe",
                age = "56",
                contact = "9426186265",
                availability = true
            )
        )
        volunteersList.add(
            Volunteer(
                name = "Jane Smith",
                age = "56",
                contact = "9426186265",
                availability = false
            )
        )
        volunteerRecyclerView.layoutManager = LinearLayoutManager(this)
        volunteerRecyclerView.adapter = VolunteersAdapter(volunteersList, onItemClick)
    }
}