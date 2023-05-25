package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.data.model.LabTestHistory
import com.medtroniclabs.opensource.databinding.RowBulletPointsBinding

class BulletPointsAdapter(
    private val list: ArrayList<LabTestHistory>
) : RecyclerView.Adapter<BulletPointsAdapter.BulletPointViewHolder>() {

    class BulletPointViewHolder(val binding: RowBulletPointsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulletPointViewHolder {
        return BulletPointViewHolder(
            RowBulletPointsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BulletPointViewHolder, position: Int) {
        val model = list[position]
        val pos = position + 1
        holder.binding.tvBullet.text = "$pos."
        holder.binding.tvTitle.text = model.labtestName
    }

    override fun getItemCount(): Int {
        return list.size
    }
}