package live.gold.rates.metal.prices.app.Adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import live.gold.rates.metal.prices.app.Model.PurityModel
import live.gold.rates.metal.prices.app.databinding.RowForRecyclerviewPurityBinding

class PurityAdaptor(
    var context: Context,
    var purityList: ArrayList<PurityModel>,
) : RecyclerView.Adapter<PurityAdaptor.viewHolder>() {

    lateinit var binding:RowForRecyclerviewPurityBinding

    class viewHolder(var binding:RowForRecyclerviewPurityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        binding= RowForRecyclerviewPurityBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        binding.purityId.text=purityList.get(position).purity
        binding.priceId.text=purityList.get(position).value
    }

    override fun getItemCount(): Int {
        return  purityList.size
    }



}