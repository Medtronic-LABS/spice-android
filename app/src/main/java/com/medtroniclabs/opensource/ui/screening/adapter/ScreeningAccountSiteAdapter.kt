package com.medtroniclabs.opensource.ui.screening.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.RowAccountSiteBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.ui.screening.listener.SiteChangeListener

class ScreeningAccountSiteAdapter(
    private val accountSiteList: ArrayList<SiteEntity>,
    val listener: SiteChangeListener,
    val selectedSite: String?
) :
    RecyclerView.Adapter<ScreeningAccountSiteAdapter.ScreeningAccountSiteViewHolder>() {

    class ScreeningAccountSiteViewHolder(val binding: RowAccountSiteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreeningAccountSiteViewHolder {
        return ScreeningAccountSiteViewHolder(
            RowAccountSiteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ScreeningAccountSiteViewHolder, position: Int) {
       val siteModel = accountSiteList[position]
       holder.binding.spinnerName.text = siteModel.name
       holder.binding.root.safeClickListener {
           listener.onSiteChange(siteModel)
       }
        if (selectedSite!=null && selectedSite.toLong() == siteModel.id){
            holder.binding.ivSelected.visibility = View.VISIBLE
        }else{
            holder.binding.ivSelected.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return accountSiteList.size
    }

}