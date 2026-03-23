package com.example.apicar.adapter
import com.example.apicar.model.Car
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apicar.R
import com.example.apicar.ui.loadUrl

class CarAdapter(
    private val cars: List<Car>,
    private val onItemClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val nameTextView: TextView = view.findViewById(R.id.name)
        val yearTextView: TextView = view.findViewById(R.id.age)
        val licenceTextView: TextView = view.findViewById(R.id.profession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]

        holder.nameTextView.text = car.name
        holder.yearTextView.text = "Ano: ${car.year}"
        holder.licenceTextView.text = "Placa: ${car.licence}"
        holder.imageView.loadUrl(car.imageUrl)

        holder.itemView.setOnClickListener {
            onItemClick(car)
        }
    }

    override fun getItemCount(): Int = cars.size
}