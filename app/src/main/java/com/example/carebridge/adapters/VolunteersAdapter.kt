import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carebridge.R
import com.example.carebridge.models.Volunteer

class VolunteersAdapter(
    private var volunteersList: List<Volunteer>,
    private val onItemClick: (Volunteer) -> Unit
) : RecyclerView.Adapter<VolunteersAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.volunteerName)
        val ageTextView: TextView = itemView.findViewById(R.id.ageText)
        val contactTextView: TextView = itemView.findViewById(R.id.contactText)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(volunteersList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_available_volunteers, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val volunteer = volunteersList[position]
        holder.nameTextView.text = volunteer.name
        holder.ageTextView.text = "Age: ${volunteer.age}"
        holder.contactTextView.text = "Contact No.: ${volunteer.contact}"
    }

    override fun getItemCount(): Int {
        return volunteersList.size
    }

    fun updateVolunteers(newVolunteers: List<Volunteer>) {
        volunteersList = newVolunteers
        notifyDataSetChanged()
    }
}