package com.medtroniclabs.opensource.ui.medicalreview

import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.databinding.InvestigationAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.adapter.ResultsAdapter

class LabTestCreateAdapter(
    private val anInterface: LabTestInterface
) :
    RecyclerView.Adapter<LabTestCreateAdapter.ViewHolder>() {

    private var adapterList = ArrayList<LabTestModel>()
    private var selectedItem: Long = -1L

    inner class ViewHolder(val binding: InvestigationAdapterBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val context: Context = binding.root.context

        fun bind(position: Int, item: LabTestModel) {

            binding.tvTestName.text = item.labTestName ?: "-"
            binding.tvRefOn.text = "-"
            binding.tvTestedOn.text = "-"

            item.referDateDisplay?.let {
                binding.tvRefOn.text = DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }

            binding.ivDot.visibility = if (item.isAbnormal == true) View.VISIBLE else View.GONE

            item.referredDate?.let {
                binding.tvRefOn.text = DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }

            item.resultDate?.let {
                binding.tvTestedOn.text = DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }
            binding.tvRefBy.text = "-"

            binding.tvRefBy.text = getReferredBy(item)

            binding.tvRefBy.setTextColor(getTextColor(context, item.referredBy))

            binding.tvValue.text = context.getString(R.string.not_available)
            getResultUpdatedBy(item)?.let {
                binding.tvValue.text = it
            }

            binding.tvValue.setTextColor(getTextColor(context, item.resultUpdateBy))

            binding.ivDelete.visibility = View.GONE
            binding.ivDropDown.visibility =
                if (item.resultDate.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.ivEdit.visibility =
                if (item.referredDate != null && item.resultDate.isNullOrEmpty()) View.VISIBLE else View.GONE

            binding.ivRemove.visibility =
                if (item.isReviewed == true || item.resultDate != null) View.GONE else View.VISIBLE

            binding.ivEdit.safeClickListener(this)
            binding.ivDropDown.safeClickListener(this)
            binding.tvTestName.safeClickListener(this)
            binding.ivRemove.safeClickListener(this)
            binding.btnReview.safeClickListener(this)

            binding.etComment.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {/**
                 * this method is not used
                 */}

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    item.resultComments = if (text.isNullOrBlank()) "" else text.toString()
                }

                override fun afterTextChanged(p0: Editable?) {/**
                 * this method is not used
                 */}
            })

            binding.ivDropDown.rotation = 0f

            setResultAdapter(context, item, binding)
        }

        override fun onClick(mView: View?) {
            when (mView?.id) {
                binding.ivRemove.id -> {
                   handleRemove(context, layoutPosition)
                }

                binding.ivEdit.id -> {
                    if (layoutPosition < adapterList.size) {
                        adapterList[layoutPosition].let {
                            anInterface.onItemSelected(
                                it,
                                isRemove = false,
                                loadResult = false
                            )
                        }
                    }
                }

                binding.tvTestName.id -> {
                    if (binding.ivDropDown.visibility == View.VISIBLE)
                        binding.ivDropDown.performClick()
                }

                binding.ivDropDown.id -> {
                    if (binding.resultsLayout.visibility == View.GONE) {
                        if (layoutPosition < adapterList.size) {
                            selectedItem = adapterList[layoutPosition]._id ?: -1L
                            adapterList[layoutPosition].let {
                                anInterface.onItemSelected(
                                    it,
                                    isRemove = false,
                                    loadResult = true
                                )
                            }
                        }
                    } else {
                        rotateArrow0f(binding.ivDropDown)
                        binding.resultsLayout.visibility = View.GONE
                    }
                }

                binding.btnReview.id -> {
                    if (layoutPosition < adapterList.size) {
                        selectedItem = adapterList[layoutPosition]._id ?: -1L
                        adapterList[layoutPosition].let {
                            anInterface.reviewResults(it)
                        }
                    }
                }
            }
        }
    }

    private fun handleRemove(context: Context, layoutPosition: Int) {
        if ((context as BaseActivity).connectivityManager.isNetworkAvailable()) {
            context.showAlertDialogWithComments(
                title = context.getString(R.string.confirmation),
                message = context.getString(R.string.delete_confirmation),
                positiveButtonName = context.getString(R.string.ok),
                isNegativeButtonNeed = true,
                negativeButtonName = context.getString(R.string.cancel),
                showComment = false
            ){ isPositiveResult, _ ->
                if (isPositiveResult && layoutPosition < adapterList.size) {
                    adapterList[layoutPosition].let {
                        adapterList.removeAt(layoutPosition)
                        notifyItemRemoved(layoutPosition)
                        anInterface.onItemSelected(
                            it,
                            isRemove = true,
                            loadResult = false
                        )
                    }
                }
            }
        } else {
            context.showErrorDialogue(
                context.getString(R.string.error),
                context.getString(R.string.no_internet_error),
                false
            ) {}
        }
    }

    private fun setResultAdapter(
        context: Context,
        item: LabTestModel,
        binding: InvestigationAdapterBinding
    ) {
        if (item._id == selectedItem) {
            binding.resultsLayout.visibility = View.VISIBLE
            rotateArrow180f(binding.ivDropDown)
            if ((item.resultDetails?.size ?: 0) > 0) {
                binding.resultsGrid.visibility = View.VISIBLE
                val resultAdapter = ResultsAdapter(item.resultDetails!!)
                binding.resultsGrid.layoutManager =
                    GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
                binding.resultsGrid.adapter = resultAdapter

                if (item.isReviewed == true) {
                    binding.reviewGrp.visibility = View.GONE
                    binding.commentLayout.root.visibility = View.VISIBLE
                    binding.commentLayout.tvKey.text = context.getString(R.string.test_comments)
                    binding.commentLayout.tvValue.text =
                        if (item.resultComments.isNullOrBlank()) "-" else item.resultComments
                } else {
                    binding.reviewGrp.visibility = View.VISIBLE
                    binding.commentLayout.root.visibility = View.GONE
                    binding.etComment.setText("")
                }
            } else
                binding.resultsLayout.visibility = View.GONE
            selectedItem = -1L
        } else {
            binding.resultsLayout.visibility = View.GONE
            item.resultDetails = null
        }
    }

    private fun getResultUpdatedBy(item: LabTestModel): String? {
        item.resultUpdateBy?.let { map ->
            var text = ""
            if (map.containsKey(DefinedParams.First_Name))
                text = map[DefinedParams.First_Name] as String
            if (map.containsKey(DefinedParams.Last_Name))
                text = "$text ${map[DefinedParams.Last_Name] as String}"
            return text
        }
        return null
    }

    private fun getReferredBy(item: LabTestModel): String {
        var referredBy = ""
        if(item.referredBy != null && item.referredBy is Map<*, *>) {
            (item.referredBy as Map<*, *>).let { map ->
                if (map.containsKey(DefinedParams.First_Name))
                    referredBy = map[DefinedParams.First_Name] as String
                if (map.containsKey(DefinedParams.Last_Name))
                    referredBy = "$referredBy ${map[DefinedParams.Last_Name] as String}"
            }
        } else {
            referredBy = item.referredByDisplay ?: ""
        }
        return referredBy
    }

    private fun rotateArrow180f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f)
        ivArrow.start()
    }

    private fun rotateArrow0f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f)
        ivArrow.start()
    }

    private fun getTextColor(context: Context, enteredBy: Any?): Int {
        return if (enteredBy == null) context.getColor(R.color.disabled_text_color) else context.getColor(
            R.color.navy_blue
        )
    }

    fun getData() = adapterList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            InvestigationAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterList.let {
            holder.bind(position, it[position])
        }
    }

    override fun getItemCount(): Int = adapterList.size

    fun submitData(list: ArrayList<LabTestModel>) {
        adapterList = ArrayList(list)
        notifyItemRangeChanged(0, adapterList.size)
    }

    fun showResultDetails(resultsData: List<Map<String, Any>>, comment: String?) {
        adapterList.find { it._id == selectedItem }?.let { model ->
            model.resultDetails = ArrayList(resultsData)
            model.resultComments = comment
        }
        notifyItemRangeChanged(0, adapterList.size)
    }

    fun updateReviewCommentsView() {
        adapterList.find { it._id == selectedItem }?.let { model ->
            model.isReviewed = true
            notifyItemRangeChanged(0, adapterList.size)
        }
    }
}