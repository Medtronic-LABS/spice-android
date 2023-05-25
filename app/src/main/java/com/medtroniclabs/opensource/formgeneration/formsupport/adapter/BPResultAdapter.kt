package com.medtroniclabs.opensource.formgeneration.formsupport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.RowBpResultBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class BPResultAdapter(private var list: ArrayList<Map<String, Double>>) :
    RecyclerView.Adapter<BPResultAdapter.BPResultViewHolder>() {

    class BPResultViewHolder(val binding: RowBpResultBinding) :
        RecyclerView.ViewHolder(binding.root){
            val context = binding.root.context!!
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPResultViewHolder {
        return BPResultViewHolder(
            RowBpResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BPResultViewHolder, position: Int) {
        val map = list[position]
        val displayIndex = position + 1
        holder.binding.tvTake.text = "$displayIndex"
        holder.binding.tvSystolic.text = holder.context.getString(R.string.sugar_append,map[DefinedParams.Systolic])
        holder.binding.tvDiastolic.text = holder.context.getString(R.string.sugar_append,map[DefinedParams.Diastolic])
        holder.binding.tvPulse.text =  holder.context.getString(R.string.pulse_append,map[DefinedParams.Pulse])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}