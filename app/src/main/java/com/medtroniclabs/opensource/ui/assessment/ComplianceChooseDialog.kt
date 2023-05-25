package com.medtroniclabs.opensource.ui.assessment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.SpinnerDropDownBinding
import com.medtroniclabs.opensource.db.tables.MedicalComplianceEntity

class ComplianceSpinnerAdapter(context: Context, val translationToggle: Boolean) :
    ArrayAdapter<String>(context, R.layout.spinner_drop_down_item_compliance) {

    var itemList = ArrayList<MedicalComplianceEntity>()

    override fun getCount(): Int = itemList.size

    fun setData(listItems: List<MedicalComplianceEntity>) {
        itemList = ArrayList(listItems)
    }

    fun getData(position: Int) : MedicalComplianceEntity? {
        return if(position<itemList.size) itemList[position] else null
    }

    override fun getItem(position: Int): String {
        return if (translationToggle){
            itemList[position].cultureValue?:itemList[position].name
        }else{
            itemList[position].name
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    private fun createView(position: Int, viewGroup: ViewGroup?): View {

        val binding = SpinnerDropDownBinding.inflate(
            LayoutInflater.from(viewGroup?.context)
        )
        binding.tvTitle.text = getItem(position)

        return binding.root
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getDropDownView(position: Int, mView: View?, parentGroup: ViewGroup): View {
        return createView(position, parentGroup)
    }
}