package com.medtroniclabs.opensource.ui.medicalreview.pharamcist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.data.model.FillMedicineResponse
import com.medtroniclabs.opensource.databinding.RowQualiPharmBinding

class QualiPharmAdapter(val list: ArrayList<FillMedicineResponse>) : RecyclerView.Adapter<QualiPharmAdapter.QualiPharmViewHolder>() {

    class QualiPharmViewHolder(val binding: RowQualiPharmBinding): RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QualiPharmViewHolder {
        return QualiPharmViewHolder(
            RowQualiPharmBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QualiPharmViewHolder, position: Int) {
        val model = list[position]
        val pos = position + 1
        holder.binding.tvBullet.text = "$pos."
        holder.binding.tvTitle.text = getMessage(model,holder.context)
    }

    private fun getMessage(model: FillMedicineResponse, context: Context): String {
         if (model.distrubutedQuantity > 0){
             return context.getString(R.string.quantity_difference_message,model.medicationName,model.distrubutedQuantity)
         }else {
             return context.getString(R.string.out_of_stock_message,model.medicationName)
         }

    }

    override fun getItemCount(): Int {
       return list.size
    }
}