package live.gold.rates.metal.prices.app.Adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SpinnerAdapter
import androidx.recyclerview.widget.RecyclerView
import live.gold.rates.metal.prices.app.databinding.RowForSpinnerBinding


class SpinnerAdaptar (var context: Context, var abstractList: ArrayList<String>) :
    RecyclerView.Adapter<SpinnerAdaptar.viewHolder>() {

    lateinit var binding: RowForSpinnerBinding

    class viewHolder(var binding: RowForSpinnerBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        binding = RowForSpinnerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolder(binding)
    }


    override fun getItemCount(): Int {
        return abstractList.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.binding.textViewName.text=abstractList.get(position).toString()
    }
}