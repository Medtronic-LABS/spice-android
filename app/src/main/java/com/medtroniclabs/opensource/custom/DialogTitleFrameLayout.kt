package com.medtroniclabs.opensource.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.R.styleable

class DialogTitleFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var ivClose: ImageView = ImageView(context)
    var titleView: TextView = TextView(context)

    init {
        initView()
        val a = context.theme.obtainStyledAttributes(
            attrs,
            styleable.DialogTitleFrameLayout,
            0, 0
        )
        try {
            a.getString(styleable.DialogTitleFrameLayout_dialogTitle)?.let {
                titleView.text = it
            }
        } finally {
            a.recycle()
        }
    }

    /**
     * init View Here
     */
    private fun initView() {
        val rootView = (context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.dialog_title, this, true)
        titleView = rootView.findViewById<TextView>(R.id.tvDialogTitle)
        ivClose = rootView.findViewById<ImageView>(R.id.ivClose)
    }
}