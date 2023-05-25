package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.LayoutResultsAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class ResultsAdapter(private var dataList: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutResultsAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    class ViewHolder(val binding: LayoutResultsAdapterBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind(position: Int, item: Map<String, Any>) {

            if(item.containsKey(DefinedParams.Result_Name)) {
                binding.tvKey.text = item[DefinedParams.Result_Name] as String? ?: "-"
            }
            val strBuilder = StringBuilder()
            if(item.containsKey(DefinedParams.Result_Value)) {
                strBuilder.append(item[DefinedParams.Result_Value] as String? ?: "")
            }
            strBuilder.append(" ")
            if(item.containsKey(DefinedParams.Unit)) {
                strBuilder.append(item[DefinedParams.Unit] as String? ?: "")
            }
            binding.tvValue.text = strBuilder.toString()

            if (item.containsKey(DefinedParams.Is_Abnormal)) {
                val isAbnormal = item[DefinedParams.Is_Abnormal]
                binding.tvValue.setTextColor(
                    if (isAbnormal is Boolean && isAbnormal) context.getColor(R.color.a_red_error) else context.getColor(
                        R.color.black
                    )
                )
            }

            if (item.containsKey(DefinedParams.Display_Name)) {
                val displayName = item[DefinedParams.Display_Name]
                if (displayName is String) {
                    binding.tvUnit.text = displayName.ifBlank { "" }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataList.let {
            holder.bind(position, it[position])
        }
    }

    override fun getItemCount(): Int = dataList.size
}