package com.medtroniclabs.opensource.formgeneration.formsupport.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.RowSpinnerSelectionBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.listener.SpinnerListener

class SpinnerSelectionAdapter(
    private val itemList: ArrayList<Map<String, Any>>,
    private val listener: SpinnerListener,
    val spinnerSelectedItemId: String?
) :
    RecyclerView.Adapter<SpinnerSelectionAdapter.SpinnerSelectionViewHolder>() {

    class SpinnerSelectionViewHolder(val binding: RowSpinnerSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerSelectionViewHolder {
        return SpinnerSelectionViewHolder(
            RowSpinnerSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SpinnerSelectionViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.spinnerName.text = item[DefinedParams.NAME] as String? ?: ""
        holder.binding.spinnerName.safeClickListener {
            listener.onSpinnerItemSelected(item)
        }
        if (item[DefinedParams.id]!=null && item[DefinedParams.id] == spinnerSelectedItemId){
            holder.binding.ivSelected.visibility = View.VISIBLE
        }else{
            holder.binding.ivSelected.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}