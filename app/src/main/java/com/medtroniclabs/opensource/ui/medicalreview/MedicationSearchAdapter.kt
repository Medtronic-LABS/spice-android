package com.medtroniclabs.opensource.ui.medicalreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.medtroniclabs.opensource.databinding.AutoCompleteAdapterBinding
import java.util.*

class MedicationSearchAdapter : BaseAdapter(), Filterable {

    private var itemList = ArrayList<String>()
    private var originalList = ArrayList<String>()

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): String = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun setData(itemList: ArrayList<String>) {
        this.itemList = itemList
        originalList = itemList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val myHolder: ViewHolder
        if (convertView == null) {
            val binding = AutoCompleteAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            myHolder = ViewHolder(binding)
            myHolder.bind(position, getItem(position))
            myHolder.binding.root.tag = myHolder
        } else
            myHolder = convertView.tag as ViewHolder

        return myHolder.binding.root
    }

    class ViewHolder(val binding: AutoCompleteAdapterBinding) : View.OnClickListener {
        override fun onClick(mView: View?) {
            when (mView) {
                binding.root -> {
                    //add name to list & show in bottom
                }
            }
        }

        fun bind(position: Int, item: String) {
            binding.tvTitle.text = item
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {
                /**
                 * this method is not used
                 */
            }

            override fun performFiltering(charSeq: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filterResults.values = originalList
                filterResults.count = originalList.size
                return filterResults
            }
        }
    }
}