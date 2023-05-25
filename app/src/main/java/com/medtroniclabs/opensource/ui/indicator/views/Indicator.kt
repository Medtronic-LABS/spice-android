package com.medtroniclabs.opensource.ui.indicator.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.ui.indicator.interfaces.IndicatorType
import com.medtroniclabs.opensource.ui.indicator.utils.SizeUtils
import com.medtroniclabs.opensource.ui.views.IndicatorSeekBar

class Indicator(
    context: Context?,
    seekBar: IndicatorSeekBar?,
    indicatorColor: Int,
    indicatorType: Int,
    indicatorTextSize: Int,
    indicatorTextColor: Int,
    indicatorCustomView: View?,
    indicatorCustomTopContentView: View?
) {
    private var mWindowWidth = 0
    private val mLocation = IntArray(2)
    private var mArrowView: ArrowView? = null
    private var mProgressTextView: TextView? = null
    private var mIndicatorPopW: PopupWindow? = null
    private var mTopContentView: LinearLayout? = null
    private var mGap = 0
    private var mIndicatorColor = indicatorColor
    private var mContext: Context? = context
    private var mIndicatorType = indicatorType
    private var mSeekBar: IndicatorSeekBar? = seekBar
    private var mIndicatorView: View? = null
    private var mIndicatorCustomView: View? = indicatorCustomView
    private var mIndicatorCustomTopContentView: View? = indicatorCustomTopContentView
    private var mIndicatorTextSize = 0f
    private var mIndicatorTextColor = indicatorTextColor

    init {
        mIndicatorTextSize = indicatorTextSize.toFloat()
        mWindowWidth = getWindowWidth()
        mGap = SizeUtils.dp2px(mContext!!, 2f)
        initIndicator()
    }

    private fun initIndicator() {
        if (mIndicatorType == IndicatorType.CUSTOM)
            if (mIndicatorCustomView != null)
                setCustomIndicator()
            else
                throw IllegalArgumentException("the attr：indicator_custom_layout must be set while you set the indicator type to CUSTOM.")
        else
            customIndicator()
    }

    private fun setCustomIndicator() {
        mIndicatorView = mIndicatorCustomView
        //for the custom indicator view, if progress need to show when seeking ,
        // need a TextView to show progress and this textView 's identify must be progress;
        val progressTextViewId = mContext!!.resources.getIdentifier(
            "isb_progress",
            "id",
            mContext!!.applicationContext.packageName
        )
        if (progressTextViewId > 0) {
            val view = mIndicatorView!!.findViewById<View>(progressTextViewId)
            view?.let {
                if (view is TextView) {
                    //progressText
                    mProgressTextView = view
                    mProgressTextView!!.text = mSeekBar!!.indicatorTextString
                    mProgressTextView!!.textSize =
                        SizeUtils.px2sp(mContext!!, mIndicatorTextSize)
                            .toFloat()
                    mProgressTextView!!.setTextColor(mIndicatorTextColor)
                } else
                    throw ClassCastException("the view identified by isb_progress in indicator custom layout can not be cast to TextView")
            }
        }
    }

    private fun customIndicator() {
        if (mIndicatorType == IndicatorType.CIRCULAR_BUBBLE) {
            mIndicatorView = CircleBubbleView(
                mContext,
                mIndicatorTextSize,
                mIndicatorTextColor,
                mIndicatorColor,
                "1000"
            )
            (mIndicatorView as CircleBubbleView?)?.setProgress(mSeekBar!!.indicatorTextString)
        } else {
            mIndicatorView = View.inflate(mContext, R.layout.isb_indicator, null)
            //container
            mTopContentView =
                mIndicatorView?.findViewById<View>(R.id.indicator_container) as LinearLayout
            //arrow
            mArrowView = mIndicatorView?.findViewById<View>(R.id.indicator_arrow) as ArrowView
            mArrowView?.setColor(mIndicatorColor)
            //progressText
            mProgressTextView = mIndicatorView?.findViewById<View>(R.id.isb_progress) as TextView
            mProgressTextView!!.text = mSeekBar!!.indicatorTextString
            mProgressTextView!!.textSize =
                SizeUtils.px2sp(mContext!!, mIndicatorTextSize).toFloat()
            mProgressTextView!!.setTextColor(mIndicatorTextColor)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mTopContentView!!.background = getGradientDrawable()
            } else {
                mTopContentView!!.setBackgroundDrawable(getGradientDrawable())
            }
            //custom top content view
            mIndicatorCustomTopContentView?.let {
                setCustomTopContentView()
            }
        }
    }

    private fun setCustomTopContentView() {
        //for the custom indicator top content view, if progress need to show when seeking ,
        //need a TextView to show progress and this textView 's identify must be progress;
        val progressTextViewId = mContext!!.resources.getIdentifier(
            "isb_progress",
            "id",
            mContext!!.applicationContext.packageName
        )
        val topContentView: View? = mIndicatorCustomTopContentView
        topContentView?.let { tpContentView ->
            if (progressTextViewId > 0) {
                val tv = tpContentView.findViewById<View>(progressTextViewId)
                if (tv != null && tv is TextView) {
                    setTopContentView(tpContentView, tv)
                } else {
                    setTopContentView(tpContentView)
                }
            } else {
                setTopContentView(tpContentView)
            }
        }
    }

    @NonNull
    private fun getGradientDrawable(): GradientDrawable? {
        val tvDrawable: GradientDrawable
        tvDrawable = if (mIndicatorType == IndicatorType.ROUNDED_RECTANGLE) {
            mContext!!.resources.getDrawable(R.drawable.isb_indicator_rounded_corners) as GradientDrawable
        } else {
            mContext!!.resources.getDrawable(R.drawable.isb_indicator_square_corners) as GradientDrawable
        }
        tvDrawable.setColor(mIndicatorColor)
        return tvDrawable
    }

    private fun getWindowWidth(): Int {
        val wm = mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return wm?.defaultDisplay?.width ?: 0
    }

    private fun getIndicatorScreenX(): Int {
        mSeekBar!!.getLocationOnScreen(mLocation)
        return mLocation[0]
    }

    private fun adjustArrow(touchX: Float) {
        if (mIndicatorType == IndicatorType.CUSTOM || mIndicatorType == IndicatorType.CIRCULAR_BUBBLE) {
            return
        }
        val indicatorScreenX = getIndicatorScreenX()
        if (indicatorScreenX + touchX < mIndicatorPopW!!.contentView.measuredWidth / 2) {
            setMargin(
                mArrowView,
                -(mIndicatorPopW!!.contentView.measuredWidth / 2 - indicatorScreenX - touchX).toInt(),
                -1,
                -1,
                -1
            )
        } else if (mWindowWidth - indicatorScreenX - touchX < mIndicatorPopW!!.contentView.measuredWidth / 2) {
            setMargin(
                mArrowView,
                (mIndicatorPopW!!.contentView.measuredWidth / 2 - (mWindowWidth - indicatorScreenX - touchX)).toInt(),
                -1,
                -1,
                -1
            )
        } else {
            setMargin(mArrowView, 0, 0, 0, 0)
        }
    }

    private fun setMargin(view: View?, left: Int, top: Int, right: Int, bottom: Int) {
        if (view == null) {
            return
        }
        if (view.layoutParams is MarginLayoutParams) {
            val layoutParams = view.layoutParams as MarginLayoutParams
            layoutParams.setMargins(
                if (left == -1) layoutParams.leftMargin else left,
                if (top == -1) layoutParams.topMargin else top,
                if (right == -1) layoutParams.rightMargin else right,
                if (bottom == -1) layoutParams.bottomMargin else bottom
            )
            view.requestLayout()
        }
    }

    fun iniPop() {
        if (mIndicatorPopW != null) {
            return
        }
        if (mIndicatorType != IndicatorType.NONE && mIndicatorView != null) {
            mIndicatorView!!.measure(0, 0)
            mIndicatorPopW = PopupWindow(
                mIndicatorView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
        }
    }

    fun getInsideContentView(): View? {
        return mIndicatorView
    }

    fun setProgressTextView(text: String?) {
        if (mIndicatorView is CircleBubbleView) {
            (mIndicatorView as CircleBubbleView?)?.setProgress(text)
        } else if (mProgressTextView != null) {
            mProgressTextView!!.text = text
        }
    }

    fun updateIndicatorLocation(offset: Int) {
        setMargin(mIndicatorView, offset, -1, -1, -1)
    }

    fun updateArrowViewLocation(offset: Int) {
        setMargin(mArrowView, offset, -1, -1, -1)
    }


    /**
     * update the indicator position
     *
     * @param touchX the x location you touch without padding left.
     */
    fun update(touchX: Float) {
        if (!mSeekBar!!.isEnabled || mSeekBar!!.visibility !== View.VISIBLE) {
            return
        }
        refreshProgressText()
        if (mIndicatorPopW != null) {
            mIndicatorPopW!!.contentView.measure(0, 0)
            mIndicatorPopW!!.update(
                mSeekBar,
                (touchX - mIndicatorPopW!!.contentView.measuredWidth / 2).toInt(),
                -(mSeekBar!!.measuredHeight + mIndicatorPopW!!.contentView.measuredHeight - mSeekBar!!.paddingTop /*- mSeekBar.getTextHeight() */ + mGap),
                -1,
                -1
            )
            adjustArrow(touchX)
        }
    }

    /**
     * call this method to show the indicator.
     *
     * @param touchX the x location you touch, padding left excluded.
     */
    fun show(touchX: Float) {
        if (!mSeekBar!!.isEnabled || mSeekBar!!.visibility !== View.VISIBLE) {
            return
        }
        refreshProgressText()
        if (mIndicatorPopW != null) {
            mIndicatorPopW!!.contentView.measure(0, 0)
            mIndicatorPopW!!.showAsDropDown(
                mSeekBar, (touchX - mIndicatorPopW!!.contentView.measuredWidth / 2f).toInt(),
                -(mSeekBar!!.measuredHeight + mIndicatorPopW!!.contentView.measuredHeight - mSeekBar!!.paddingTop /*- mSeekBar.getTextHeight()*/ + mGap)
            )
            adjustArrow(touchX)
        }
    }

    fun refreshProgressText() {
        val tickTextString = mSeekBar!!.indicatorTextString
        if (mIndicatorView is CircleBubbleView) {
            (mIndicatorView as CircleBubbleView?)?.setProgress(tickTextString)
        } else if (mProgressTextView != null) {
            mProgressTextView!!.text = tickTextString
        }
    }

    /**
     * call this method hide the indicator
     */
    fun hide() {
        if (mIndicatorPopW == null) {
            return
        }
        mIndicatorPopW!!.dismiss()
    }

    fun isShowing(): Boolean {
        return mIndicatorPopW != null && mIndicatorPopW!!.isShowing
    }


    /*----------------------API START-------------------*/

    /*----------------------API START-------------------*/
    /**
     * get the indicator content view.
     *
     * @return the view which is inside indicator.
     */
    fun getContentView(): View? {
        return mIndicatorView
    }

    /**
     * call this method to replace the current indicator with a new indicator view , indicator arrow will be replace ,too.
     *
     * @param customIndicatorView a new content view for indicator.
     */
    fun setContentView(customIndicatorView: View?) {
        mIndicatorType = IndicatorType.CUSTOM
        mIndicatorCustomView = customIndicatorView
        initIndicator()
    }

    /**
     * call this method to replace the current indicator with a new indicator view, indicator arrow will be replace ,too.
     *
     * @param customIndicatorView a new content view for indicator.
     * @param progressTextView    this TextView will show the progress or tick text, must be found in @param customIndicatorView
     */
    fun setContentView(customIndicatorView: View?, progressTextView: TextView?) {
        mProgressTextView = progressTextView
        mIndicatorType = IndicatorType.CUSTOM
        mIndicatorCustomView = customIndicatorView
        initIndicator()
    }

    /**
     * get the indicator top content view.
     * if indicator type [IndicatorType] is CUSTOM or CIRCULAR_BUBBLE, call this method will get a null value.
     *
     * @return the view which is inside indicator's top part, not include arrow
     */
    fun getTopContentView(): View? {
        return mTopContentView
    }

    /**
     * set the View to the indicator top container, not influence indicator arrow ;
     * if indicator type [IndicatorType] is CUSTOM or CIRCULAR_BUBBLE, call this method will be not worked.
     *
     * @param topContentView the view is inside the indicator TOP part, not influence indicator arrow;
     */
    fun setTopContentView(@NonNull topContentView: View) {
        setTopContentView(topContentView, null)
    }

    /**
     * set the  View to the indicator top container, and show the changing progress in indicator when seek;
     * not influence indicator arrow;
     * if indicator type is custom , this method will be not work.
     *
     * @param topContentView   the view is inside the indicator TOP part, not influence indicator arrow;
     * @param progressTextView this TextView will show the progress or tick text, must be found in @param topContentView
     */
    fun setTopContentView(@NonNull topContentView: View, @Nullable progressTextView: TextView?) {
        mProgressTextView = progressTextView
        mTopContentView!!.removeAllViews()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            topContentView.background = getGradientDrawable()
        } else {
            topContentView.setBackgroundDrawable(getGradientDrawable())
        }
        mTopContentView!!.addView(topContentView)
    }
}