package com.medtroniclabs.opensource.ui.boarding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.RowSiteListBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity

class SiteAdapter(
    private val siteList: List<SiteEntity>,
    private var siteSelected: SiteEntity?,
    private val siteSelectionCallback: ((siteEntity: SiteEntity) -> Unit)
) :
    RecyclerView.Adapter<SiteAdapter.SiteViewHolder>() {

    class SiteViewHolder(val binding: RowSiteListBinding) : RecyclerView.ViewHolder(binding.root) {
        val viewContext: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        return SiteViewHolder(
            RowSiteListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val siteModel = siteList[position]
        holder.binding.tvSiteName.text = siteModel.name
        holder.binding.tvRole.text = siteModel.roleName

        holder.binding.root.safeClickListener {
            siteSelected = siteModel
            notifyItemRangeChanged(0, siteList.size)
        }

        siteSelected?.let {
            if (siteModel.id == it.id) {
                siteSelectionCallback.invoke(siteModel)
                holder.binding.ivChecked.visibility = View.VISIBLE
                holder.binding.tvSiteName.isEnabled = true
                holder.binding.tvRole.isEnabled = true
                holder.binding.tvSiteName.typeface =
                    ResourcesCompat.getFont(holder.viewContext, R.font.inter_bold)
                holder.binding.tvRole.typeface =
                    ResourcesCompat.getFont(holder.viewContext, R.font.inter_regular)
            } else {
                holder.binding.ivChecked.visibility = View.GONE
                holder.binding.tvSiteName.isEnabled = false
                holder.binding.tvRole.isEnabled = false
                holder.binding.tvSiteName.typeface =
                    ResourcesCompat.getFont(holder.viewContext, R.font.inter_regular)
                holder.binding.tvRole.typeface =
                    ResourcesCompat.getFont(holder.viewContext, R.font.inter_regular)
            }
        } ?: run {
            holder.binding.ivChecked.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return siteList.size
    }
}