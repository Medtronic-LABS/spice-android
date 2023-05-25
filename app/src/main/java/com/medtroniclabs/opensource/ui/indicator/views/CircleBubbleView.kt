package com.medtroniclabs.opensource.ui.indicator.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import com.medtroniclabs.opensource.ui.indicator.utils.SizeUtils

class CircleBubbleView : View {
    private var mIndicatorTextColor = 0
    private var mIndicatorColor = 0
    private var mIndicatorTextSize = 0f
    private var mContext: Context? = null
    private var mPath: Path? = null
    private var mPaint: Paint? = null
    private var mIndicatorWidth = 0f
    private var mIndicatorHeight = 0f
    private var mTextHeight = 0f
    private var mProgress: String? = null

    @JvmOverloads
    internal constructor(
        context: Context?,
        @Nullable attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init("100")
    }

    internal constructor(
        context: Context?,
        indicatorTextSize: Float,
        indicatorTextColor: Int,
        indicatorColor: Int,
        maxLengthText: String
    ) : super(context, null, 0) {
        mContext = context
        mIndicatorTextSize = indicatorTextSize
        mIndicatorTextColor = indicatorTextColor
        mIndicatorColor = indicatorColor
        init(maxLengthText)
    }

    private fun init(maxLengthText: String) {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeWidth = 1f
        mPaint!!.textAlign = Paint.Align.CENTER
        mPaint!!.textSize = mIndicatorTextSize
        val mRect = Rect()
        mPaint!!.getTextBounds(maxLengthText, 0, maxLengthText.length, mRect)
        mIndicatorWidth = (mRect.width() + SizeUtils.dp2px(mContext!!, 4f)).toFloat()
        val minWidth = SizeUtils.dp2px(mContext!!, 36f)
        if (mIndicatorWidth < minWidth) {
            mIndicatorWidth = minWidth.toFloat()
        }
        mTextHeight = mRect.height().toFloat()
        mIndicatorHeight = mIndicatorWidth * 1.2f
        initPath()
    }

    private fun initPath() {
        mPath = Path()
        val rectF = RectF(0f, 0f, mIndicatorWidth, mIndicatorWidth)
        mPath!!.arcTo(rectF, 135f, 270f)
        mPath!!.lineTo(mIndicatorWidth / 2, mIndicatorHeight)
        mPath!!.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mIndicatorWidth.toInt(), mIndicatorHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        mPaint!!.color = mIndicatorColor
        canvas.drawPath(mPath!!, mPaint!!)
        mPaint!!.color = mIndicatorTextColor
        canvas.drawText(
            mProgress!!, mIndicatorWidth / 2f, mIndicatorHeight / 2 + mTextHeight / 4,
            mPaint!!
        )
    }

    fun setProgress(progress: String?) {
        mProgress = progress
        invalidate()
    }
}