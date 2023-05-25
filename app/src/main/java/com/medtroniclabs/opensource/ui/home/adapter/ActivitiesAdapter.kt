package com.medtroniclabs.opensource.ui.home.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.RowActivitiesBinding
import com.medtroniclabs.opensource.db.tables.MenuEntity
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.home.MenuSelectionListener

class ActivitiesAdapter(
    private val roleBasedActivitiesList: List<MenuEntity>,
    private val listener: MenuSelectionListener,
    val translate: Boolean = false
) :
    RecyclerView.Adapter<ActivitiesAdapter.ActivitiesViewHolder>() {

    class ActivitiesViewHolder(val binding: RowActivitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        return ActivitiesViewHolder(
            RowActivitiesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        val model = roleBasedActivitiesList[position]
        if (translate){
            holder.binding.tvTitle.text = model.cultureValue?:model.name
        }else{
            holder.binding.tvTitle.text = model.name
        }
        val imageModel = getResourceActivityId(
            model.menuId,
            holder.context
        )
        if (imageModel == null){
            holder.binding.ivActivity.visibility = View.INVISIBLE
        }else{
            holder.binding.ivActivity.visibility = View.VISIBLE
            holder.binding.ivActivity.setImageDrawable(imageModel)
        }

        holder.binding.root.safeClickListener {
            listener.onMenuSelected(model.menuId)
        }
    }


    private fun getResourceActivityId(model: String, context: Context): Drawable? {
        return when (model) {
            UIConstants.screeningUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_screening_icon
            )
            UIConstants.AssessmentUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_assessment
            )
            UIConstants.enrollmentUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_enrollemnt
            )

            UIConstants.MedicalReviewUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_screening_home
            )
            UIConstants.PrescriptionUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_medication
            )
            UIConstants.myPatientsUniqueID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_my_patient
            )
            UIConstants.investigation -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_investigation
            )
            UIConstants.lifestyle -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_lifestyle_icon)
            else -> null
        }
    }

    override fun getItemCount(): Int {
        return roleBasedActivitiesList.size
    }
}