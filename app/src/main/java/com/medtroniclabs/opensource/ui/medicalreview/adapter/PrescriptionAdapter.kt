package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.PrescriptionAdapterBinding

class PrescriptionAdapter(context: Context) : ArrayAdapter<Pair<String, String>>(context, R.layout.spinner_drop_down_item) {

    private var itemList = ArrayList<Pair<String, String>>()

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Pair<String, String> = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun setData(itemList: ArrayList<Pair<String, String>>) {
        this.itemList = itemList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val myHolder: ViewHolder

        if (convertView == null) {
            val binding = PrescriptionAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            view = binding.root
            myHolder = ViewHolder(binding)
            myHolder.bind(getItem(position))
            view.tag = myHolder
        } else{
            view = convertView
            myHolder = convertView.tag as ViewHolder
            myHolder.bind(getItem(position))
        }

        return view
    }

    class ViewHolder(val binding: PrescriptionAdapterBinding) {
        fun bind(item: Pair<String, String>) {

            binding.tvTitle.text = item.first
            binding.tvClassification.text = item.second
        }
    }
}