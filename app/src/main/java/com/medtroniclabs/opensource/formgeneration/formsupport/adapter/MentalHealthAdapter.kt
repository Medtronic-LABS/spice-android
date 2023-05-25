package com.medtroniclabs.opensource.formgeneration.formsupport.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.ui.MentalHealthOption
import com.medtroniclabs.opensource.databinding.RowMentalHealthBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class MentalHealthAdapter(
    val list: ArrayList<MentalHealthOption>,
    val baseId: String,
    val editList: ArrayList<Map<String, Any>>? = null,
    val isViewOnly: Boolean = false,
    val translate: Boolean = false,
    private val callback: ((question: String, questionId: Long, option: String, id: String, answerId: Long, displayOrder: Long) -> Unit?)? = null) :
    RecyclerView.Adapter<MentalHealthAdapter.MentalHealthViewHolder>() {

    class MentalHealthViewHolder(val binding: RowMentalHealthBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentalHealthViewHolder {
        return MentalHealthViewHolder(
            RowMentalHealthBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MentalHealthViewHolder, position: Int) {
        val model = list[position]
        val question = (model.map[DefinedParams.Questions] as String?) ?: ""
        val questionCulture = (model.map[DefinedParams.cultureValue] as String?)?:""
        val questionId = (model.map[DefinedParams.ID] as Double?)?.toLong() ?: 0L
        val displayOrder = (model.map[DefinedParams.Display_Order] as? Number)?.toLong() ?: -1
        setOptionsTagId(holder, model, question, questionId, displayOrder)
        if (translate){
             if (questionCulture.isNotBlank()){
                 holder.binding.tvQuestion.text = questionCulture
             } else{
                 holder.binding.tvQuestion.text = question
             }
        }else {
            holder.binding.tvQuestion.text = question
        }
        val isMandatory = (model.map[DefinedParams.Mandatory] as Boolean?) ?: false
        if (isMandatory) {
            holder.binding.tvQuestion.markMandatory()
        }
        holder.binding.btnNotAtAll.isSelected =
            model.selectedOption != null && model.selectedOption.equals(DefinedParams.NotAtAll)
        holder.binding.btnMoreThanHalfDay.isSelected =
            model.selectedOption != null && model.selectedOption.equals(DefinedParams.MoreThanHalfADay)
        holder.binding.btnSeveralDays.isSelected =
            model.selectedOption != null && model.selectedOption.equals(DefinedParams.SeveralDays)
        holder.binding.btnNearlyEveryDay.isSelected =
            model.selectedOption != null && model.selectedOption.equals(DefinedParams.NearlyEveryDay)
        if (!isViewOnly) {
            holder.binding.btnNotAtAll.safeClickListener {
                model.selectedOption = DefinedParams.NotAtAll
                notifyItemChanged(position)
                val answerId = (it?.tag as Long?) ?: -1L
                callback?.invoke(question, questionId, DefinedParams.NotAtAll, baseId, answerId, displayOrder)
            }
            holder.binding.btnMoreThanHalfDay.safeClickListener {
                model.selectedOption = DefinedParams.MoreThanHalfADay
                notifyItemChanged(position)
                val answerId = (it?.tag as Long?) ?: -1L
                callback?.invoke(question, questionId, DefinedParams.MoreThanHalfADay, baseId, answerId, displayOrder)
            }
            holder.binding.btnSeveralDays.safeClickListener {
                model.selectedOption = DefinedParams.SeveralDays
                notifyItemChanged(position)
                val answerId = (it?.tag as Long?) ?: -1L
                callback?.invoke(question, questionId, DefinedParams.SeveralDays, baseId, answerId, displayOrder)
            }
            holder.binding.btnNearlyEveryDay.safeClickListener {
                model.selectedOption = DefinedParams.NearlyEveryDay
                notifyItemChanged(position)
                val answerId = (it?.tag as Long?) ?: -1L
                callback?.invoke(question, questionId, DefinedParams.NearlyEveryDay, baseId, answerId, displayOrder)
            }
        }
    }

    private fun markSelection(
        holder: MentalHealthViewHolder,
        selectedAnswerName: String
    ) {
        when (selectedAnswerName.lowercase()) {
            DefinedParams.NotAtAll.lowercase() -> {
                holder.binding.btnNotAtAll.isSelected = true
            }
            DefinedParams.SeveralDays.lowercase() -> {
                holder.binding.btnSeveralDays.isSelected = true
            }
            DefinedParams.MoreThanHalfADay.lowercase() -> {
                holder.binding.btnMoreThanHalfDay.isSelected = true
            }
            DefinedParams.NearlyEveryDay.lowercase() -> {
                holder.binding.btnNearlyEveryDay.isSelected = true
            }
        }
    }

    private fun setOptionsTagId(
        holder: MentalHealthViewHolder,
        model: MentalHealthOption,
        question: String,
        questionId: Long? = null,
        displayOrder: Long
    ) {
        var answerMap: Map<String,Any>? = null
        if (model.map.containsKey(DefinedParams.ModelAnswers) && model.map[DefinedParams.ModelAnswers] is List<*>) {
            val optionsList =
                model.map[DefinedParams.ModelAnswers] as List<*>//as List<Map<String,Any>>

            optionsList.forEach { element ->
                if (element is Map<*, *> && element.containsKey(DefinedParams.Answer) && element.containsKey(
                        DefinedParams.ID
                    )
                ) {
                    (element[DefinedParams.Answer] as String).let { name ->
                        (element[DefinedParams.ID] as Double).toLong().let { id ->
                            setName(holder, name.lowercase(), id)
                            if (answerMap == null && (editList?.size ?: 0) > 0) {
                                editLists(
                                    model,
                                    holder,
                                    name,
                                    displayOrder,
                                    id,
                                    questionId,
                                    question
                                )?.let { ansMap ->
                                    answerMap = ansMap
                                }
                            }

                            answerMap?.let {
                                editList?.remove(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun editLists(
        model: MentalHealthOption,
        holder: MentalHealthViewHolder,
        name: String,
        displayOrder: Long,
        id: Long,
        questionId: Long?,
        question: String
    ): Map<String, Any>? {
        var answerMap: Map<String, Any>? = null
        editList?.forEach edit@{ editMap ->
            if (editMap.containsKey(DefinedParams.Question_Id)
                && editMap.containsKey(DefinedParams.Answer_Id)
                && (editMap[DefinedParams.Question_Id] as Double?)?.toLong() == questionId
                && (editMap[DefinedParams.Answer_Id] as Double?)?.toLong() == id
            ) {
                answerMap = editMap
                model.selectedOption = getAnswerName(name)
                markSelection(holder, name)
                callback?.invoke(
                    question,
                    questionId ?: 0L,
                    model.selectedOption!!,
                    baseId,
                    id,
                    displayOrder
                )
                return@edit
            }
        }
        return answerMap
    }

    private fun setName(holder: MentalHealthViewHolder, name: String, id: Long) {
        when (name) {
            DefinedParams.NotAtAll.lowercase() -> {
                holder.binding.btnNotAtAll.tag = id
            }
            DefinedParams.SeveralDays.lowercase() -> {
                holder.binding.btnSeveralDays.tag = id
            }
            DefinedParams.MoreThanHalfADay.lowercase() -> {
                holder.binding.btnMoreThanHalfDay.tag = id
            }
            DefinedParams.NearlyEveryDay.lowercase() -> {
                holder.binding.btnNearlyEveryDay.tag = id
            }
        }
    }

    private fun getAnswerName(name: String): String {
        return when (name.lowercase()) {
            DefinedParams.NotAtAll.lowercase() -> {
                DefinedParams.NotAtAll
            }
            DefinedParams.SeveralDays.lowercase() -> {
                DefinedParams.SeveralDays
            }
            DefinedParams.MoreThanHalfADay.lowercase() -> {
                DefinedParams.MoreThanHalfADay
            }
            DefinedParams.NearlyEveryDay.lowercase() -> {
                DefinedParams.NearlyEveryDay
            }
            else -> ""
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}