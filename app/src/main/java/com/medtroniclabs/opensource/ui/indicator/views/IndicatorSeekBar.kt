package com.medtroniclabs.opensource.ui.views

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetParcelable
import com.medtroniclabs.opensource.ui.indicator.interfaces.ColorCollector
import com.medtroniclabs.opensource.ui.indicator.interfaces.IndicatorType
import com.medtroniclabs.opensource.ui.indicator.interfaces.OnSeekChangeListener
import com.medtroniclabs.opensource.ui.indicator.interfaces.TickMarkType
import com.medtroniclabs.opensource.ui.indicator.utils.FormatUtils
import com.medtroniclabs.opensource.ui.indicator.utils.SizeUtils
import com.medtroniclabs.opensource.ui.indicator.views.Builder
import com.medtroniclabs.opensource.ui.indicator.views.Indicator
import com.medtroniclabs.opensource.ui.indicator.views.SeekParams
import java.lang.reflect.Method
import java.math.BigDecimal

class IndicatorSeekBar : View {
    private var mContext: Context
    private var mStockPaint //the paint for seek bar drawing
            : Paint? = null
    private var mTextPaint //the paint for mTickTextsArr drawing
            : TextPaint? = null
    private var mSeekChangeListener: OnSeekChangeListener? = null
    private var mRect: Rect? = null
    private var mCustomDrawableMaxHeight //the max height for custom drawable
            = 0f
    private var lastProgress = 0f
    private var mFaultTolerance = -1f //the tolerance for user seek bar touching
    private var mScreenWidth = -1f
    private var mClearPadding = false
    private var mSeekParams //save the params when seeking change.
            : SeekParams? = null

    //seek bar
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var mMeasuredWidth = 0
    private var mPaddingTop = 0
    private var mSeekLength //the total length of seek bar
            = 0f
    private var mSeekBlockLength //the length for each section part to seek
            = 0f
    private var mIsTouching //user is touching the seek bar
            = false
    private var mMax = 0f
    private var mMin = 0f
    private var mProgress = 0f
    private var mIsFloatProgress // true for the progress value in float,otherwise in int.
            = false
    private var mScale = 1 //the scale of the float progress.
    private var mUserSeekable //true if the user can seek to change the progress,otherwise only can be changed by setProgress().
            = false
    private var mOnlyThumbDraggable //only drag the seek bar's thumb can be change the progress
            = false
    private var mSeekSmoothly //seek continuously
            = false
    private var mIsAboveUpperLimit //append character at the end
            = false
    private var mProgressArr = floatArrayOf()//save the progress which at tickMark position.
    private var mR2L //right to left,compat local problem.
            = false

    //tick texts
    private var mShowTickText //the palace where the tick text show .
            = false
    private var mShowBothTickTextsOnly //show the tick texts on the both ends of seek bar before.
            = false
    private var mTickTextsHeight //the height of text
            = 0
    private var mTickTextsArr //save the tick texts which at tickMark position.
            : Array<String?>? = null
    private var mTickTextsWidth = floatArrayOf() //save the tick texts width bounds.
    private var mTextCenterX = floatArrayOf() //the text's drawing X anchor
    private var mTickTextY //the text's drawing Y anchor
            = 0f
    private var mTickTextsSize = 0
    private var mTextsTypeface //the tick texts and thumb texts' typeface
            : Typeface? = null
    private var mSelectedTextsColor //the color for the tick texts those thumb swept.
            = 0
    private var mUnselectedTextsColor //the color for the tick texts those thumb haven't reach.
            = 0
    private var mHoveredTextColor //the color for the tick texts which below/above thumb.
            = 0
    private var mTickTextsCustomArray: Array<CharSequence>? = null

    /**
     * get current indicator
     *
     * @return the indicator
     */
    //indicator
    var indicator //the pop window above the seek bar
            : Indicator? = null
        private set
    private var mIndicatorColor = 0
    private var mIndicatorTextColor = 0
    private var mIndicatorStayAlways //true if the indicator didn't dismiss after initial.
            = false
    private var mIndicatorTextSize = 0
    var indicatorContentView //the view to replace the raw indicator all view
            : View? = null
        private set
    private var mIndicatorTopContentView //the view to replace the raw indicator content view
            : View? = null
    private var mShowIndicatorType //different indicator type.
            = 0
    private var mIndicatorTextFormat: String? = null

    //tick marks
    private var mTickMarksX //the tickMark's drawing X anchor
            : FloatArray? = null
    private var mTicksCount //the num of tickMarks
            = 0
    private var mUnSelectedTickMarksColor //the color for the tickMarks those thumb haven't reach.
            = 0
    private var mSelectedTickMarksColor //the color for the tickMarks those thumb swept.
            = 0
    private var mTickRadius //the tick's radius
            = 0f
    private var mUnselectTickMarksBitmap //the drawable bitmap for tick
            : Bitmap? = null
    private var mSelectTickMarksBitmap //the drawable bitmap for tick
            : Bitmap? = null
    private var mTickMarksDrawable: Drawable? = null
    private var mShowTickMarksType = 0
    private var mTickMarksEndsHide //true if want to hide the tickMarks which in both side ends of seek bar
            = false
    private var mTickMarksSweptHide //true if want to hide the tickMarks which on thumb left.
            = false
    private var mTickMarksSize //the width of tickMark
            = 0

    //track
    private var mTrackRoundedCorners = false
    private var mProgressTrack //the background track on the thumb start
            : RectF? = null
    private var mBackgroundTrack //the background track on the thumb ends
            : RectF? = null
    private var mBackgroundTrackSize = 0
    private var mProgressTrackSize = 0
    private var mBackgroundTrackColor = 0
    private var mProgressTrackColor = 0
    private var mSectionTrackColorArray = intArrayOf() //save the color for each section tracks.
    private var mCustomTrackSectionColorResult //true to confirm to custom the section track color
            = false

    //thumb
    private var mThumbRadius //the thumb's radius
            = 0f
    private var mThumbTouchRadius //the thumb's radius when touching
            = 0f
    private var mThumbBitmap //the drawable bitmap for thumb
            : Bitmap? = null
    private var mThumbColor = 0
    private var mThumbSize = 0
    private var mThumbDrawable: Drawable? = null
    private var mPressedThumbBitmap //the bitmap for pressing status
            : Bitmap? = null
    private var mPressedThumbColor //the color for pressing status
            = 0

    //thumb text
    private var mShowThumbText //the place where the thumb text show .
            = false
    private var mThumbTextY //the thumb text's drawing Y anchor
            = 0f
    private var mThumbTextColor = 0
    private var mHideThumb = false
    private var mAdjustAuto = false

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        mContext = context
        initAttrs(mContext, attrs)
        initParams()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        initAttrs(mContext, attrs)
        initParams()
    }

    /**
     * if you want a java build, the way like:
     *
     *
     * IndicatorSeekBar
     * .with(getContext())
     * .max(50)
     * .min(10)
     * ...
     * .build();
     */
    constructor(builder: Builder) : super(builder.context) {
        mContext = builder.context
        val defaultPadding: Int = SizeUtils.dp2px(mContext, 16f)
        setPadding(defaultPadding, paddingTop, defaultPadding, paddingBottom)
        this.apply(builder)
        initParams()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val builder = Builder(context)
        if (attrs == null) {
            this.apply(builder)
            return
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorSeekBar)
        //seekBar
        mMax = ta.getFloat(R.styleable.IndicatorSeekBar_isb_max, builder.max)
        mMin = ta.getFloat(R.styleable.IndicatorSeekBar_isb_min, builder.min)
        mProgress = ta.getFloat(R.styleable.IndicatorSeekBar_isb_progress, builder.progress)
        mIsFloatProgress = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_progress_value_float,
            builder.progressValueFloat
        )
        mUserSeekable =
            ta.getBoolean(R.styleable.IndicatorSeekBar_isb_user_seekable, builder.userSeekable)
        mClearPadding = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_clear_default_padding,
            builder.clearPadding
        )
        mOnlyThumbDraggable = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_only_thumb_draggable,
            builder.onlyThumbDraggable
        )
        mSeekSmoothly =
            ta.getBoolean(R.styleable.IndicatorSeekBar_isb_seek_smoothly, builder.seekSmoothly)
        mIsAboveUpperLimit =
            ta.getBoolean(R.styleable.IndicatorSeekBar_isb_is_above_upper_limit, builder.isAboveUpperLimit)
        mR2L = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_r2l, builder.r2l)
        //track
        mBackgroundTrackSize = ta.getDimensionPixelSize(
            R.styleable.IndicatorSeekBar_isb_track_background_size,
            builder.trackBackgroundSize
        )
        mProgressTrackSize = ta.getDimensionPixelSize(
            R.styleable.IndicatorSeekBar_isb_track_progress_size,
            builder.trackProgressSize
        )
        mBackgroundTrackColor = ta.getColor(
            R.styleable.IndicatorSeekBar_isb_track_background_color,
            builder.trackBackgroundColor
        )
        mProgressTrackColor = ta.getColor(
            R.styleable.IndicatorSeekBar_isb_track_progress_color,
            builder.trackProgressColor
        )
        mTrackRoundedCorners = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_track_rounded_corners,
            builder.trackRoundedCorners
        )
        //thumb
        mThumbSize =
            ta.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_thumb_size, builder.thumbSize)
        mThumbDrawable = ta.getDrawable(R.styleable.IndicatorSeekBar_isb_thumb_drawable)
        mAdjustAuto = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_thumb_adjust_auto, true)
        initThumbColor(
            ta.getColorStateList(R.styleable.IndicatorSeekBar_isb_thumb_color),
            builder.thumbColor
        )
        //thumb text
        mShowThumbText =
            ta.getBoolean(R.styleable.IndicatorSeekBar_isb_show_thumb_text, builder.showThumbText)
        mThumbTextColor =
            ta.getColor(R.styleable.IndicatorSeekBar_isb_thumb_text_color, builder.thumbTextColor)
        //tickMarks
        mTicksCount = ta.getInt(R.styleable.IndicatorSeekBar_isb_ticks_count, builder.tickCount)
        mShowTickMarksType = ta.getInt(
            R.styleable.IndicatorSeekBar_isb_show_tick_marks_type,
            builder.showTickMarksType
        )
        mTickMarksSize = ta.getDimensionPixelSize(
            R.styleable.IndicatorSeekBar_isb_tick_marks_size,
            builder.tickMarksSize
        )
        initTickMarksColor(
            ta.getColorStateList(R.styleable.IndicatorSeekBar_isb_tick_marks_color),
            builder.tickMarksColor
        )
        mTickMarksDrawable = ta.getDrawable(R.styleable.IndicatorSeekBar_isb_tick_marks_drawable)
        mTickMarksSweptHide = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_tick_marks_swept_hide,
            builder.tickMarksSweptHide
        )
        mTickMarksEndsHide = ta.getBoolean(
            R.styleable.IndicatorSeekBar_isb_tick_marks_ends_hide,
            builder.tickMarksEndsHide
        )
        //tickTexts
        mShowTickText =
            ta.getBoolean(R.styleable.IndicatorSeekBar_isb_show_tick_texts, builder.showTickText)
        mTickTextsSize = ta.getDimensionPixelSize(
            R.styleable.IndicatorSeekBar_isb_tick_texts_size,
            builder.tickTextsSize
        )
        initTickTextsColor(
            ta.getColorStateList(R.styleable.IndicatorSeekBar_isb_tick_texts_color),
            builder.tickTextsColor
        )
        mTickTextsCustomArray = ta.getTextArray(R.styleable.IndicatorSeekBar_isb_tick_texts_array)
        initTextsTypeface(
            ta.getInt(R.styleable.IndicatorSeekBar_isb_tick_texts_typeface, -1),
            builder.tickTextsTypeFace
        )
        //indicator
        mShowIndicatorType =
            ta.getInt(R.styleable.IndicatorSeekBar_isb_show_indicator, builder.showIndicatorType)
        mIndicatorColor =
            ta.getColor(R.styleable.IndicatorSeekBar_isb_indicator_color, builder.indicatorColor)
        mIndicatorTextSize = ta.getDimensionPixelSize(
            R.styleable.IndicatorSeekBar_isb_indicator_text_size,
            builder.indicatorTextSize
        )
        mIndicatorTextColor = ta.getColor(
            R.styleable.IndicatorSeekBar_isb_indicator_text_color,
            builder.indicatorTextColor
        )
        val indicatorContentViewId =
            ta.getResourceId(R.styleable.IndicatorSeekBar_isb_indicator_content_layout, 0)
        if (indicatorContentViewId > 0) {
            indicatorContentView = inflate(mContext, indicatorContentViewId, null)
        }
        val indicatorTopContentLayoutId =
            ta.getResourceId(R.styleable.IndicatorSeekBar_isb_indicator_top_content_layout, 0)
        if (indicatorTopContentLayoutId > 0) {
            mIndicatorTopContentView = inflate(mContext, indicatorTopContentLayoutId, null)
        }
        ta.recycle()
    }

    private fun initParams() {
        initProgressRangeValue()
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mBackgroundTrackSize = mProgressTrackSize
        }
        if (mThumbDrawable == null) {
            mThumbRadius = mThumbSize / 2.0f
            mThumbTouchRadius = mThumbRadius * 1.2f
        } else {
            mThumbRadius =
                Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH.toFloat()), mThumbSize) / 2.0f
            mThumbTouchRadius = mThumbRadius
        }
        mTickRadius = if (mTickMarksDrawable == null) {
            mTickMarksSize / 2.0f
        } else {
            Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH.toFloat()), mTickMarksSize) / 2.0f
        }
        mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f
        initStrokePaint()
        measureTickTextsBonds()
        lastProgress = mProgress
        collectTicksInfo()
        mProgressTrack = RectF()
        mBackgroundTrack = RectF()
        initDefaultPadding()
        initIndicatorContentView()
    }

    private fun collectTicksInfo() {
        require(!(mTicksCount < 0 || mTicksCount > 50)) { "the Argument: TICK COUNT must be limited between (0-50), Now is $mTicksCount" }
        if (mTicksCount != 0) {
            mTickMarksX = FloatArray(mTicksCount)
            if (mShowTickText) {
                mTextCenterX = FloatArray(mTicksCount)
                mTickTextsWidth = FloatArray(mTicksCount)
            }
            mProgressArr = FloatArray(mTicksCount)
            for (i in mProgressArr.indices) {
                mProgressArr[i] =
                    mMin + i * (mMax - mMin) / if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
            }
        }
    }

    private fun initDefaultPadding() {
        if (!mClearPadding) {
            val normalPadding: Int = SizeUtils.dp2px(mContext, 16f)
            if (paddingLeft == 0) {
                setPadding(normalPadding, paddingTop, paddingRight, paddingBottom)
            }
            if (paddingRight == 0) {
                setPadding(paddingLeft, paddingTop, normalPadding, paddingBottom)
            }
        }
    }

    private fun initProgressRangeValue() {
        require(mMax >= mMin) { "the Argument: MAX's value must be larger than MIN's." }
        if (mProgress < mMin) {
            mProgress = mMin
        }
        if (mProgress > mMax) {
            mProgress = mMax
        }
    }

    private fun initStrokePaint() {
        if (mStockPaint == null) {
            mStockPaint = Paint()
        }
        if (mTrackRoundedCorners) {
            mStockPaint!!.strokeCap = Paint.Cap.ROUND
        }
        mStockPaint!!.isAntiAlias = true
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mProgressTrackSize = mBackgroundTrackSize
        }
    }

    private fun measureTickTextsBonds() {
        if (needDrawText()) {
            initTextPaint()
            mTextPaint!!.typeface = mTextsTypeface
            mTextPaint!!.getTextBounds("j", 0, 1, mRect)
            mTickTextsHeight = mRect!!.height() + SizeUtils.dp2px(
                mContext,
                3f
            ) //with the gap(3dp) between tickTexts and track.
        }
    }

    private fun needDrawText(): Boolean {
        return mShowThumbText || mTicksCount != 0 && mShowTickText
    }

    private fun initTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = TextPaint()
            mTextPaint!!.isAntiAlias = true
            mTextPaint!!.textAlign = Paint.Align.CENTER
            mTextPaint!!.textSize = mTickTextsSize.toFloat()
        }
        if (mRect == null) {
            mRect = Rect()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = Math.round(mCustomDrawableMaxHeight + paddingTop + paddingBottom)
        setMeasuredDimension(
            resolveSize(SizeUtils.dp2px(mContext, 170f), widthMeasureSpec),
            height + mTickTextsHeight
        )
        initSeekBarInfo()
        refreshSeekBarLocation()
    }

    private fun initSeekBarInfo() {
        mMeasuredWidth = measuredWidth
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mPaddingLeft = paddingLeft
            mPaddingRight = paddingRight
        } else {
            mPaddingLeft = paddingStart
            mPaddingRight = paddingEnd
        }
        mPaddingTop = paddingTop
        mSeekLength = (mMeasuredWidth - mPaddingLeft - mPaddingRight).toFloat()
        mSeekBlockLength = mSeekLength / if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
    }

    private fun refreshSeekBarLocation() {
        initTrackLocation()
        //init TickTexts Y Location
        if (needDrawText()) {
            mTextPaint!!.getTextBounds("j", 0, 1, mRect)
            mTickTextY =
                mPaddingTop + mCustomDrawableMaxHeight + Math.round(mRect!!.height() - mTextPaint!!.descent()) + SizeUtils.dp2px(
                    mContext,
                    3f
                )
            mThumbTextY = mTickTextY
        }
        //init tick's X and text's X location;
        if (mTickMarksX == null) {
            return
        }
        initTextsArray()
        //adjust thumb auto,so find out the closest progress in the mProgressArr array and replace it.
        //it is not necessary to adjust thumb while count is less than 2.
        if (mTicksCount > 2) {
            mProgress = mProgressArr[closestIndex]
            lastProgress = mProgress
        }
        refreshThumbCenterXByProgress(mProgress)
    }

    private fun initTextsArray() {
        if (mTicksCount == 0) {
            return
        }
        if (mShowTickText) {
            mTickTextsArr = arrayOfNulls(mTicksCount)
        }
        for (i in mTickMarksX!!.indices) {
            if (mShowTickText) {
                mTickTextsArr!![i] = getTickTextByPosition(i)
                mTextPaint!!.getTextBounds(
                    mTickTextsArr!![i],
                    0,
                    mTickTextsArr!![i]!!.length,
                    mRect
                )
                mTickTextsWidth[i] = mRect!!.width().toFloat()
                mTextCenterX[i] = mPaddingLeft + mSeekBlockLength * i
            }
            mTickMarksX!![i] = mPaddingLeft + mSeekBlockLength * i
        }
    }

    private fun initTrackLocation() {
        if (mR2L) {
            mBackgroundTrack!!.left = mPaddingLeft.toFloat()
            mBackgroundTrack!!.top = mPaddingTop + mThumbTouchRadius
            //ThumbCenterX
            mBackgroundTrack!!.right =
                mPaddingLeft + mSeekLength * (1.0f - (mProgress - mMin) / amplitude)
            mBackgroundTrack!!.bottom = mBackgroundTrack!!.top
            //ThumbCenterX
            mProgressTrack!!.left = mBackgroundTrack!!.right
            mProgressTrack!!.top = mBackgroundTrack!!.top
            mProgressTrack!!.right = (mMeasuredWidth - mPaddingRight).toFloat()
            mProgressTrack!!.bottom = mBackgroundTrack!!.bottom
        } else {
            mProgressTrack!!.left = mPaddingLeft.toFloat()
            mProgressTrack!!.top = mPaddingTop + mThumbTouchRadius
            //ThumbCenterX
            mProgressTrack!!.right = (mProgress - mMin) * mSeekLength / amplitude + mPaddingLeft
            mProgressTrack!!.bottom = mProgressTrack!!.top
            //ThumbCenterX
            mBackgroundTrack!!.left = mProgressTrack!!.right
            mBackgroundTrack!!.top = mProgressTrack!!.bottom
            mBackgroundTrack!!.right = (mMeasuredWidth - mPaddingRight).toFloat()
            mBackgroundTrack!!.bottom = mProgressTrack!!.bottom
        }
    }

    private fun getTickTextByPosition(index: Int): String {
        if (mTickTextsCustomArray == null) {
            return getProgressString(mProgressArr[index])
        }
        return if (index < mTickTextsCustomArray!!.size) {
            mTickTextsCustomArray!!.get(index).toString()
        } else ""
    }

    /**
     * calculate the thumb's centerX by the changing progress.
     */
    private fun refreshThumbCenterXByProgress(progress: Float) {
        //ThumbCenterX
        if (mR2L) {
            mBackgroundTrack!!.right =
                mPaddingLeft + mSeekLength * (1.0f - (progress - mMin) / amplitude) //ThumbCenterX
            mProgressTrack!!.left = mBackgroundTrack!!.right
        } else {
            mProgressTrack!!.right = (progress - mMin) * mSeekLength / amplitude + mPaddingLeft
            mBackgroundTrack!!.left = mProgressTrack!!.right
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawTickMarks(canvas)
        drawTickTexts(canvas)
        drawThumb(canvas)
        drawThumbText(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        if (mCustomTrackSectionColorResult) { //the track has custom the section track color
            val sectionSize = if (mTicksCount - 1 > 0) mTicksCount - 1 else 1
            for (i in 0 until sectionSize) {
                if (mR2L) {
                    mStockPaint!!.color = mSectionTrackColorArray[sectionSize - i - 1]
                } else {
                    mStockPaint!!.color = mSectionTrackColorArray[i]
                }
                val thumbPosFloat = thumbPosOnTickFloat
                if (i < thumbPosFloat && thumbPosFloat < i + 1) {
                    //the section track include the thumb,
                    // set the ProgressTrackSize for thumb's left side track ,
                    // BGTrackSize for the right's.
                    val thumbCenterX = thumbCenterX
                    mStockPaint!!.strokeWidth = leftSideTrackSize.toFloat()
                    canvas.drawLine(
                        mTickMarksX!![i],
                        mProgressTrack!!.top,
                        thumbCenterX,
                        mProgressTrack!!.bottom,
                        mStockPaint!!
                    )
                    mStockPaint!!.strokeWidth = rightSideTrackSize.toFloat()
                    canvas.drawLine(
                        thumbCenterX, mProgressTrack!!.top,
                        mTickMarksX!![i + 1], mProgressTrack!!.bottom,
                        mStockPaint!!
                    )
                } else {
                    thumbFloat(i, thumbPosFloat)
                    canvas.drawLine(
                        mTickMarksX!![i],
                        mProgressTrack!!.top,
                        mTickMarksX!![i + 1],
                        mProgressTrack!!.bottom,
                        mStockPaint!!
                    )
                }
            }
        } else {
            //draw progress track
            mStockPaint!!.color = mProgressTrackColor
            mStockPaint!!.strokeWidth = mProgressTrackSize.toFloat()
            canvas.drawLine(
                mProgressTrack!!.left,
                mProgressTrack!!.top,
                mProgressTrack!!.right,
                mProgressTrack!!.bottom,
                mStockPaint!!
            )
            //draw BG track
            mStockPaint!!.color = mBackgroundTrackColor
            mStockPaint!!.strokeWidth = mBackgroundTrackSize.toFloat()
            canvas.drawLine(
                mBackgroundTrack!!.left,
                mBackgroundTrack!!.top,
                mBackgroundTrack!!.right,
                mBackgroundTrack!!.bottom,
                mStockPaint!!
            )
        }
    }

    private fun thumbFloat(i: Int, thumbPosFloat: Float) {
        if (i < thumbPosFloat) {
            mStockPaint!!.strokeWidth = leftSideTrackSize.toFloat()
        } else {
            mStockPaint!!.strokeWidth = rightSideTrackSize.toFloat()
        }
    }

    private fun drawTickMarks(canvas: Canvas) {
        if (canDrawTick()) {
            return
        }
        for (i in mTickMarksX!!.indices) {
            val thumbPosFloat = thumbPosOnTickFloat
            if (mTickMarksSweptHide && thumbCenterX >= mTickMarksX!![i]) {
                continue
            }
            if (mTickMarksEndsHide && (i == 0 || i == mTickMarksX!!.size - 1)) {
                continue
            }
            if (i == thumbPosOnTick && mTicksCount > 2 && !mSeekSmoothly) {
                continue
            }
            newThumbFloat(i, thumbPosFloat)
            if (mTickMarksDrawable != null) {
                drawTick()
                newThumbFloat(i, thumbPosFloat, true, canvas)
                continue
            }
            tickMarkType(i, canvas)
        }
    }

    private fun drawTick() {
        if (mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null) {
            initTickMarksBitmap()
        }
        require(!(mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null)) {
            //please check your selector drawable's format and correct.
            "the format of the selector TickMarks drawable is wrong!"
        }
    }

    private fun newThumbFloat(
        i: Int,
        thumbPosFloat: Float,
        drawBitmap: Boolean? = false,
        canvas: Canvas? = null
    ) {
        if (i <= thumbPosFloat) {
            if (drawBitmap == true)
                canvas?.drawBitmap(
                    mSelectTickMarksBitmap!!,
                    mTickMarksX!![i] - mUnselectTickMarksBitmap!!.width / 2.0f,
                    mProgressTrack!!.top - mUnselectTickMarksBitmap!!.height / 2.0f,
                    mStockPaint
                )
            else
                mStockPaint!!.color = leftSideTickColor
        } else {
            if (drawBitmap == true)
                canvas?.drawBitmap(
                    mUnselectTickMarksBitmap!!,
                    mTickMarksX!![i] - mUnselectTickMarksBitmap!!.width / 2.0f,
                    mProgressTrack!!.top - mUnselectTickMarksBitmap!!.height / 2.0f,
                    mStockPaint
                )
            else
                mStockPaint!!.color = rightSideTickColor
        }
    }

    private fun canDrawTick(): Boolean {
        return mTicksCount == 0 || mShowTickMarksType == TickMarkType.NONE && mTickMarksDrawable == null
    }

    private fun tickMarkType(i: Int, canvas: Canvas) {
        when (mShowTickMarksType) {
            TickMarkType.OVAL -> {
                canvas.drawCircle(
                    mTickMarksX!![i], mProgressTrack!!.top, mTickRadius,
                    mStockPaint!!
                )
            }
            TickMarkType.DIVIDER -> {
                val rectWidth: Int = SizeUtils.dp2px(mContext, 1f)
                var dividerTickHeight: Float
                dividerTickHeight = if (thumbCenterX >= mTickMarksX!![i]) {
                    leftSideTrackSize.toFloat()
                } else {
                    rightSideTrackSize.toFloat()
                }
                canvas.drawRect(
                    mTickMarksX!![i] - rectWidth,
                    mProgressTrack!!.top - dividerTickHeight / 2.0f,
                    mTickMarksX!![i] + rectWidth,
                    mProgressTrack!!.top + dividerTickHeight / 2.0f,
                    mStockPaint!!
                )
            }
            TickMarkType.SQUARE -> {
                canvas.drawRect(
                    mTickMarksX!![i] - mTickMarksSize / 2.0f,
                    mProgressTrack!!.top - mTickMarksSize / 2.0f,
                    mTickMarksX!![i] + mTickMarksSize / 2.0f,
                    mProgressTrack!!.top + mTickMarksSize / 2.0f,
                    mStockPaint!!
                )
            }
        }
    }

    private fun drawTickTexts(canvas: Canvas) {
        if (mTickTextsArr == null) {
            return
        }
        val thumbPosFloat = thumbPosOnTickFloat
        for (i in mTickTextsArr!!.indices) {
            if (mShowBothTickTextsOnly && (i != 0 && i != mTickTextsArr!!.size - 1)) {
                    continue
            }
            tickTexts(i, thumbPosFloat)
            var index = i
            if (mR2L) {
                index = mTickTextsArr!!.size - i - 1
            }
            if (i == 0) {
                canvas.drawText(
                    mTickTextsArr!![index]!!,
                    mTextCenterX[i] + mTickTextsWidth[index] / 2.0f,
                    mTickTextY,
                    mTextPaint!!
                )
            } else if (i == mTickTextsArr!!.size - 1) {
                canvas.drawText(
                    mTickTextsArr!![index]!! + if(mIsAboveUpperLimit) "+" else "",
                    mTextCenterX[i] - mTickTextsWidth[index] / 2.0f,
                    mTickTextY,
                    mTextPaint!!
                )
            } else {
                canvas.drawText(
                    mTickTextsArr!![index]!!, mTextCenterX[i], mTickTextY,
                    mTextPaint!!
                )
            }
        }
    }

    private fun tickTexts(i: Int, thumbPosFloat: Float) {
        when {
            i == thumbPosOnTick && i.toFloat() == thumbPosFloat -> {
                mTextPaint!!.color = mHoveredTextColor
            }
            i < thumbPosFloat -> {
                mTextPaint!!.color = leftSideTickTextsColor
            }
            else -> {
                mTextPaint!!.color = rightSideTickTextsColor
            }
        }
    }

    private fun drawThumb(canvas: Canvas) {
        if (mHideThumb) {
            return
        }
        val thumbCenterX = thumbCenterX
        if (mThumbDrawable != null) { //check user has set thumb drawable or not.ThumbDrawable first, thumb color for later.
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                initThumbBitmap()
            }
            require(!(mThumbBitmap == null || mPressedThumbBitmap == null)) {
                //please check your selector drawable's format and correct.
                "the format of the selector thumb drawable is wrong!"
            }
            mStockPaint!!.alpha = 255
            if (mIsTouching) {
                canvas.drawBitmap(
                    mPressedThumbBitmap!!,
                    thumbCenterX - mPressedThumbBitmap!!.width / 2.0f,
                    mProgressTrack!!.top - mPressedThumbBitmap!!.height / 2.0f,
                    mStockPaint
                )
            } else {
                canvas.drawBitmap(
                    mThumbBitmap!!,
                    thumbCenterX - mThumbBitmap!!.width / 2.0f,
                    mProgressTrack!!.top - mThumbBitmap!!.height / 2.0f,
                    mStockPaint
                )
            }
        } else {
            if (mIsTouching) {
                mStockPaint!!.color = mPressedThumbColor
            } else {
                mStockPaint!!.color = mThumbColor
            }
            canvas.drawCircle(
                thumbCenterX,
                mProgressTrack!!.top,
                if (mIsTouching) mThumbTouchRadius else mThumbRadius,
                mStockPaint!!
            )
        }
    }

    private fun drawThumbText(canvas: Canvas) {
        if (!mShowThumbText || mShowTickText && mTicksCount > 2) {
            return
        }
        mTextPaint!!.color = mThumbTextColor
        canvas.drawText(getProgressString(mProgress), thumbCenterX, mThumbTextY, mTextPaint!!)
    }

    private val thumbCenterX: Float
        get() = if (mR2L) {
            mBackgroundTrack!!.right
        } else mProgressTrack!!.right
    private val leftSideTickColor: Int
        get() {
            return if (mR2L) {
                mUnSelectedTickMarksColor
            } else mSelectedTickMarksColor
        }
    private val rightSideTickColor: Int
        get() {
            return if (mR2L) {
                mSelectedTickMarksColor
            } else mUnSelectedTickMarksColor
        }
    private val leftSideTickTextsColor: Int
        get() {
            return if (mR2L) {
                mUnselectedTextsColor
            } else mSelectedTextsColor
        }
    private val rightSideTickTextsColor: Int
        get() {
            return if (mR2L) {
                mSelectedTextsColor
            } else mUnselectedTextsColor
        }

    /**
     * get the track size which on the thumb left in R2L/L2R case.
     */
    private val leftSideTrackSize: Int
        get() {
            return if (mR2L) {
                mBackgroundTrackSize
            } else mProgressTrackSize
        }

    /**
     * get the track size which on the thumb right in R2L/L2R case.
     */
    private val rightSideTrackSize: Int
        get() {
            return if (mR2L) {
                mProgressTrackSize
            } else mBackgroundTrackSize
        }

    //when tick count = 0 ; seek bar has not tick(continuous series), return 0;
    private val thumbPosOnTick: Int
        get() {
            return if (mTicksCount != 0) {
                Math.round((thumbCenterX - mPaddingLeft) / mSeekBlockLength)
            } else 0
            //when tick count = 0 ; seek bar has not tick(continuous series), return 0;
        }

    private val thumbPosOnTickFloat: Float
        get() {
            return if (mTicksCount != 0) {
                (thumbCenterX - mPaddingLeft) / mSeekBlockLength
            } else 0f
        }

    private fun getHeightByRatio(drawable: Drawable, width: Int): Int {
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        return Math.round(1.0f * width * intrinsicHeight / intrinsicWidth)
    }

    private fun getDrawBitmap(drawable: Drawable?, isThumb: Boolean): Bitmap? {
        if (drawable == null) {
            return null
        }
        var width: Int
        var height: Int
        val maxRange: Int = SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH.toFloat())
        val intrinsicWidth = drawable.intrinsicWidth
        if (intrinsicWidth > maxRange) {
            width = if (isThumb) {
                mThumbSize
            } else {
                mTickMarksSize
            }
            height = getHeightByRatio(drawable, width)
            if (width > maxRange) {
                width = maxRange
                height = getHeightByRatio(drawable, width)
            }
        } else {
            width = drawable.intrinsicWidth
            height = drawable.intrinsicHeight
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * initial the color for the thumb.
     *
     *
     *
     *
     * NOTICE: make sure the format of color selector you set is right.
     * int[][] states = colorStateList.getStates();
     * (1) if the states.length == 1,the way you set the thumb color like :
     * app:isb_thumb_color="#XXXXXX"  or
     * app:isb_thumb_color="@color/color_name" ;
     *
     *
     * (2) if the states.length == 3,the way you set the thumb color like :
     * app:isb_thumb_color="@color/selector_color_file_name". the file(located at res/color/)'s format should like:
     *
     *
     *
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:color="#555555" android:state_pressed="true"></item>
     * <item android:color="#555555"></item>
    </selector> *
     *
     *
     * (3) if the states.length == other, the color's format you set is not support.
     */
    private fun initThumbColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the thumb color, set a default color.
        if (colorStateList == null) {
            mThumbColor = defaultColor
            mPressedThumbColor = mThumbColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field[colorStateList] as Array<IntArray>
                }
                if ("mColors" == field.name) {
                    colors = field[colorStateList] as IntArray
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parseing thumb selector color.")
        }
        thumbColorInit(states, colors)
    }

    private fun thumbColorInit(states: Array<IntArray>, colors: IntArray) {
        when (states.size) {
            1 -> {
                mThumbColor = colors[0]
                mPressedThumbColor = mThumbColor
            }
            2 -> {
                for (i in states.indices) {
                    val attr = states[i]
                    if (attr.size == 0) { //didn't have state,so just get color.
                        mPressedThumbColor = colors[i]
                        continue
                    }
                    mThumbColor = when (attr[0]) {
                        android.R.attr.state_pressed -> colors[i]
                        else -> throw IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.")
                    }
                }
            }
            else -> {
                //the color selector file was set by a wrong format , please see above to correct.
                throw IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.")
            }
        }
    }

    /**
     * initial the color for the tick masks
     *
     *
     *
     *
     * NOTICE: make sure the format of color selector you set is right.
     * int[][] states = colorStateList.getStates();
     * (1) if the states.length == 1,the way you set the tick marks' color like :
     * app:isb_tick_marks_color="#XXXXXX"  or
     * app:isb_tick_marks_color="@color/color_name" ;
     *
     *
     * (2) if the states.length == 2,the way you set the tick marks' color like :
     * app:isb_tick_marks_color="@color/selector_color_file_name". the file(located at res/color/)'s format should like:
     *
     *
     *
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:color="#555555" android:state_selected="true"></item>
     * <item android:color="#555555"></item>
    </selector> *
     *
     *
     * (3) if the states.length == other, the way you set is not support.
     */
    private fun initTickMarksColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the tick's text color, set a default selector color file.
        if (colorStateList == null) {
            mSelectedTickMarksColor = defaultColor
            mUnSelectedTickMarksColor = mSelectedTickMarksColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field[colorStateList] as Array<IntArray>
                }
                if ("mColors" == field.name) {
                    colors = field[colorStateList] as IntArray
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parsing thumb selector color." + e.message)
        }
        tickMarkColorInit(states, colors)
    }

    private fun tickMarkColorInit(states: Array<IntArray>, colors: IntArray) {
        when (states.size) {
            1 -> {
                mSelectedTickMarksColor = colors[0]
                mUnSelectedTickMarksColor = mSelectedTickMarksColor
            }
            2 -> {
                for (i in states.indices) {
                    val attr = states[i]
                    if (attr.size == 0) { //didn't have state,so just get color.
                        mUnSelectedTickMarksColor = colors[i]
                        continue
                    }
                    mSelectedTickMarksColor = when (attr[0]) {
                        android.R.attr.state_selected -> colors[i]
                        else -> throw IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.")
                    }
                }
            }
            else -> {
                //the color selector file was set by a wrong format , please see above to correct.
                throw IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.")
            }
        }
    }

    /**
     * initial the color for the tick texts.
     *
     *
     *
     *
     * NOTICE: make sure the format of color selector you set is right.
     * int[][] states = colorStateList.getStates();
     * (1) if the states.length == 1,the way you set the tick texts' color like :
     * app:isb_tick_text_color="#XXXXXX"  or
     * app:isb_tick_text_color="@color/color_name" ;
     *
     *
     * (2) if the states.length == 3,the way you set the tick texts' color like :
     * app:isb_tick_text_color="@color/selector_color_file_name". the file(located at res/color/)'s format should like:
     *
     *
     *
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:color="#555555" android:state_selected="true"></item>
     * <item android:color="#FF4081" android:state_hovered="true"></item>
     * <item android:color="#555555"></item>
    </selector> *
     *
     *
     * (3) if the states.length == other, the way you set is not support.
     */
    private fun initTickTextsColor(colorStateList: ColorStateList?, defaultColor: Int) {
        //if you didn't set the tick's texts color, will be set a selector color file default.
        if (colorStateList == null) {
            mUnselectedTextsColor = defaultColor
            mSelectedTextsColor = mUnselectedTextsColor
            mHoveredTextColor = mUnselectedTextsColor
            return
        }
        var states: Array<IntArray>? = null
        var colors: IntArray? = null
        val aClass: Class<out ColorStateList> = colorStateList.javaClass
        try {
            val f = aClass.declaredFields
            for (field in f) {
                field.isAccessible = true
                if ("mStateSpecs" == field.name) {
                    states = field[colorStateList] as Array<IntArray>
                }
                if ("mColors" == field.name) {
                    colors = field[colorStateList] as IntArray
                }
            }
            if (states == null || colors == null) {
                return
            }
        } catch (e: Exception) {
            throw RuntimeException("Something wrong happened when parseing thumb selector color.")
        }
        tickTextColorInit(states, colors)
    }

    private fun tickTextColorInit(states: Array<IntArray>, colors: IntArray) {
        when (states.size) {
            1 -> {
                mUnselectedTextsColor = colors[0]
                mSelectedTextsColor = mUnselectedTextsColor
                mHoveredTextColor = mUnselectedTextsColor
            }
            3 -> {
                for (i in states.indices) {
                    val attr = states[i]
                    if (attr.size == 0) { //didn't have state,so just get color.
                        mUnselectedTextsColor = colors[i]
                        continue
                    }
                    when (attr[0]) {
                        android.R.attr.state_selected -> mSelectedTextsColor = colors[i]
                        android.R.attr.state_hovered -> mHoveredTextColor = colors[i]
                        else -> throw IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.")
                    }
                }
            }
            else -> {
                //the color selector file was set by a wrong format , please see above to correct.
                throw IllegalArgumentException("the selector color file you set for the argument: isb_tick_texts_color is in wrong format.")
            }
        }
    }

    /**
     * initial both the tick texts' and thumb text's typeface,just has 4 type to choose,
     * but you can set the CUSTOM typeface you want by java code.
     *
     *
     * usage like:
     * indicatorSeekbar.customTickTextsTypeface(Typeface.xxx);
     */
    private fun initTextsTypeface(typeface: Int, defaultTypeface: Typeface?) {
        when (typeface) {
            0 -> mTextsTypeface = Typeface.DEFAULT
            1 -> mTextsTypeface = Typeface.MONOSPACE
            2 -> mTextsTypeface = Typeface.SANS_SERIF
            3 -> mTextsTypeface = Typeface.SERIF
            else -> {
                mTextsTypeface = defaultTypeface ?: Typeface.DEFAULT
            }
        }
    }

    /**
     * initial the bitmap for the thumb.
     *
     *
     *
     *
     * NOTICE: make sure the format of drawable selector file you set is right.
     * int stateCount = listDrawable.getStateCount();
     * (1) if the drawable instanceof BitmapDrawable,the way you set like :
     * app:isb_thumb_drawable="@drawable/ic_launcher"
     *
     *
     * (2) if the drawable instanceof StateListDrawable,the way you set like :
     * app:isb_thumb_drawable="@drawable/selector_thumb_drawable". the file(located at res/drawable/)'s format should like:
     *
     *
     *
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:drawable="@drawable/ic_launcher" android:state_pressed="true"></item>
     * <item android:drawable="@drawable/ic_launcher_round"></item>
    </selector> *
     */
    private fun initThumbBitmap() {
        if (mThumbDrawable == null) {
            return
        }
        if (mThumbDrawable is StateListDrawable) {
            try {
                val listDrawable = mThumbDrawable as StateListDrawable
                val aClass: Class<out StateListDrawable> = listDrawable.javaClass
                val stateCount = aClass.getMethod("getStateCount").invoke(listDrawable) as Int
                if (stateCount == 2) {
                    val getStateSet = aClass.getMethod(
                        "getStateSet",
                        Int::class.javaPrimitiveType
                    )
                    val getStateDrawable = aClass.getMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
                    for (i in 0 until stateCount) {
                        thumbBitmapInit(
                            getStateSet.invoke(listDrawable, i) as IntArray,
                            getStateDrawable,
                            i,
                            listDrawable
                        )
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw IllegalArgumentException("the format of the selector thumb drawable is wrong!")
                }
            } catch (e: Exception) {
                mThumbBitmap = getDrawBitmap(mThumbDrawable, true)
                mPressedThumbBitmap = mThumbBitmap
            }
        } else {
            mThumbBitmap = getDrawBitmap(mThumbDrawable, true)
            mPressedThumbBitmap = mThumbBitmap
        }
    }

    private fun thumbBitmapInit(
        stateSet: IntArray,
        getStateDrawable: Method,
        i: Int,
        listDrawable: StateListDrawable
    ) {
        if (stateSet.isNotEmpty()) {
            mPressedThumbBitmap = if (stateSet[0] == android.R.attr.state_pressed) {
                val stateDrawable =
                    getStateDrawable.invoke(listDrawable, i) as Drawable
                getDrawBitmap(stateDrawable, true)
            } else {
                //please check your selector drawable's format, please see above to correct.
                throw IllegalArgumentException("the state of the selector thumb drawable is wrong!")
            }
        } else {
            val stateDrawable = getStateDrawable.invoke(listDrawable, i) as Drawable
            mThumbBitmap = getDrawBitmap(stateDrawable, true)
        }
    }

    /**
     * initial the bitmap for the thickMarks.
     *
     *
     *
     *
     * NOTICE: make sure the format of drawable selector file you set is right.
     * int stateCount = listDrawable.getStateCount();
     * (1) if the drawable instanceof BitmapDrawable,the way you set like :
     * app:isb_tick_marks_drawable="@drawable/ic_launcher"
     *
     *
     * (2) if the drawable instanceof StateListDrawable,the way you set like :
     * app:isb_tick_marks_drawable="@drawable/selector_thumb_drawable". the file(located at res/drawable/)'s format should like:
     *
     *
     *
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:drawable="@drawable/ic_launcher" android:state_selected="true"></item>
     * <item android:drawable="@drawable/ic_launcher_round"></item>
    </selector> *
     */
    private fun initTickMarksBitmap() {
        if (mTickMarksDrawable is StateListDrawable) {
            val listDrawable = mTickMarksDrawable as StateListDrawable
            try {
                val aClass: Class<out StateListDrawable> = listDrawable.javaClass
                val getStateCount = aClass.getMethod("getStateCount")
                val stateCount = getStateCount.invoke(listDrawable) as Int
                if (stateCount == 2) {
                    val getStateSet = aClass.getMethod(
                        "getStateSet",
                        Int::class.javaPrimitiveType
                    )
                    val getStateDrawable = aClass.getMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
                    for (i in 0 until stateCount) {
                        tickMarkBitmapInit(
                            getStateSet.invoke(listDrawable, i) as IntArray,
                            getStateDrawable,
                            i,
                            listDrawable
                        )
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw IllegalArgumentException("the format of the selector TickMarks drawable is wrong!")
                }
            } catch (e: Exception) {
                mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false)
                mSelectTickMarksBitmap = mUnselectTickMarksBitmap
            }
        } else {
            mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false)
            mSelectTickMarksBitmap = mUnselectTickMarksBitmap
        }
    }

    private fun tickMarkBitmapInit(
        stateSet: IntArray,
        getStateDrawable: Method,
        i: Int,
        listDrawable: StateListDrawable
    ) {
        if (stateSet.size > 0) {
            mSelectTickMarksBitmap =
                if (stateSet[0] == android.R.attr.state_selected) {
                    val stateDrawable =
                        getStateDrawable.invoke(listDrawable, i) as Drawable
                    getDrawBitmap(stateDrawable, false)
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw IllegalArgumentException("the state of the selector TickMarks drawable is wrong!")
                }
        } else {
            val stateDrawable = getStateDrawable.invoke(listDrawable, i) as Drawable
            mUnselectTickMarksBitmap = getDrawBitmap(stateDrawable, false)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) {
            return
        }
        super.setEnabled(enabled)
        if (isEnabled) {
            alpha = 1.0f
            if (mIndicatorStayAlways) {
                indicatorContentView!!.alpha = 1.0f
            }
        } else {
            alpha = 0.3f
            if (mIndicatorStayAlways) {
                indicatorContentView!!.alpha = 0.3f
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { requestLayout() }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val parent = parent ?: return super.dispatchTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(
                false
            )
        }
        return super.dispatchTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("isb_instance_state", super.onSaveInstanceState())
        bundle.putFloat("isb_progress", mProgress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            setProgress(bundle.getFloat("isb_progress"))
            super.onRestoreInstanceState(bundle.customGetParcelable("isb_instance_state"))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mUserSeekable || !isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                val mX = event.x
                if (isTouchSeekBar(mX, event.y)) {
                    if (mOnlyThumbDraggable && !isTouchThumb(mX)) {
                        return false
                    }
                    mIsTouching = true
                    if (mSeekChangeListener != null) {
                        mSeekChangeListener?.onStartTrackingTouch(this)
                    }
                    refreshSeekBar(event)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> refreshSeekBar(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsTouching = false
                mSeekChangeListener?.onStopTrackingTouch(this)
                if (!autoAdjustThumb()) {
                    invalidate()
                }
                indicator?.hide()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun refreshSeekBar(event: MotionEvent) {
        refreshThumbCenterXByProgress(calculateProgress(calculateTouchX(adjustTouchX(event))))
        setSeekListener(true)
        invalidate()
        updateIndicator()
    }

    private fun progressChange(): Boolean {
        return if (mIsFloatProgress) {
            lastProgress != mProgress
        } else {
            Math.round(lastProgress) != Math.round(mProgress)
        }
    }

    private fun adjustTouchX(event: MotionEvent): Float {
        val mTouchXCache: Float
        mTouchXCache = if (event.x < mPaddingLeft) {
            mPaddingLeft.toFloat()
        } else if (event.x > mMeasuredWidth - mPaddingRight) {
            (mMeasuredWidth - mPaddingRight).toFloat()
        } else {
            event.x
        }
        return mTouchXCache
    }

    private fun calculateProgress(touchX: Float): Float {
        lastProgress = mProgress
        mProgress = mMin + amplitude * (touchX - mPaddingLeft) / mSeekLength
        return mProgress
    }

    private fun calculateTouchX(touchX: Float): Float {
        var touchXTemp = touchX
        //make sure the seek bar to seek smoothly always
        // while the tick's count is less than 3(tick's count is 1 or 2.).
        if (mTicksCount > 2 && !mSeekSmoothly) {
            val touchBlockSize = Math.round((touchX - mPaddingLeft) / mSeekBlockLength)
            touchXTemp = mSeekBlockLength * touchBlockSize + mPaddingLeft
        }
        return if (mR2L) {
            mSeekLength - touchXTemp + 2 * mPaddingLeft
        } else touchXTemp
    }

    private fun isTouchSeekBar(mX: Float, mY: Float): Boolean {
        if (mFaultTolerance == -1f) {
            mFaultTolerance = SizeUtils.dp2px(mContext, 5f).toFloat()
        }
        val inWidthRange =
            mX >= mPaddingLeft - 2 * mFaultTolerance && mX <= mMeasuredWidth - mPaddingRight + 2 * mFaultTolerance
        val inHeightRange =
            mY >= mProgressTrack!!.top - mThumbTouchRadius - mFaultTolerance && mY <= mProgressTrack!!.top + mThumbTouchRadius + mFaultTolerance
        return inWidthRange && inHeightRange
    }

    private fun isTouchThumb(mX: Float): Boolean {
        val rawTouchX: Float
        refreshThumbCenterXByProgress(mProgress)
        rawTouchX = if (mR2L) {
            mBackgroundTrack!!.right
        } else {
            mProgressTrack!!.right
        }
        return rawTouchX - mThumbSize / 2f <= mX && mX <= rawTouchX + mThumbSize / 2f
    }

    private fun updateIndicator() {
        if (mIndicatorStayAlways) {
            updateStayIndicator()
        } else {
            if (indicator == null) {
                return
            }
            indicator!!.iniPop()
            if (indicator!!.isShowing()) {
                indicator!!.update(thumbCenterX)
            } else {
                indicator!!.show(thumbCenterX)
            }
        }
    }

    private fun initIndicatorContentView() {
        if (mShowIndicatorType == IndicatorType.NONE) {
            return
        }
        if (indicator == null) {
            indicator = Indicator(
                mContext,
                this,
                mIndicatorColor,
                mShowIndicatorType,
                mIndicatorTextSize,
                mIndicatorTextColor,
                indicatorContentView,
                mIndicatorTopContentView
            )
            indicatorContentView = indicator!!.getInsideContentView()
        }
    }

    private fun updateStayIndicator() {
        if (!mIndicatorStayAlways || indicator == null) {
            return
        }
        indicator!!.setProgressTextView(indicatorTextString)
        indicatorContentView!!.measure(0, 0)
        val measuredWidth = indicatorContentView!!.measuredWidth
        val thumbCenterX = thumbCenterX
        if (mScreenWidth == -1f) {
            val metric = DisplayMetrics()
            val systemService: WindowManager? =
                mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (systemService != null) {
                systemService.defaultDisplay.getMetrics(metric)
                mScreenWidth = metric.widthPixels.toFloat()
            }
        }
        val indicatorOffset: Int
        val arrowOffset: Int
        if (measuredWidth / 2 + thumbCenterX > mMeasuredWidth) {
            indicatorOffset = mMeasuredWidth - measuredWidth
            arrowOffset = (thumbCenterX - indicatorOffset - measuredWidth / 2).toInt()
        } else if (thumbCenterX - measuredWidth / 2 < 0) {
            indicatorOffset = 0
            arrowOffset = -(measuredWidth / 2 - thumbCenterX).toInt()
        } else {
            indicatorOffset = (thumbCenterX - measuredWidth / 2).toInt()
            arrowOffset = 0
        }
        indicator!!.updateIndicatorLocation(indicatorOffset)
        indicator!!.updateArrowViewLocation(arrowOffset)
    }

    private fun autoAdjustThumb(): Boolean {
        if (mTicksCount < 3 || !mSeekSmoothly) { //it is not necessary to adjust while count less than 2.
            return false
        }
        if (!mAdjustAuto) {
            return false
        }
        val closestIndex = closestIndex
        val touchUpProgress = mProgress
        val animator =
            ValueAnimator.ofFloat(0f, Math.abs(touchUpProgress - mProgressArr[closestIndex]))
        animator.start()
        animator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                lastProgress = mProgress
                mProgress = if (touchUpProgress - mProgressArr[closestIndex] > 0) {
                    touchUpProgress - animation.animatedValue as Float
                } else {
                    touchUpProgress + animation.animatedValue as Float
                }
                refreshThumbCenterXByProgress(mProgress)
                //the auto adjust was happened after user touched up, so from user is false.
                setSeekListener(false)
                if (indicator != null && mIndicatorStayAlways) {
                    indicator!!.refreshProgressText()
                    updateStayIndicator()
                }
                invalidate()
            }
        })
        return true
    }

    /**
     * transfer the progress value to string type
     */
    private fun getProgressString(progress: Float): String {
        return if (mIsFloatProgress) {
            FormatUtils.fastFormat(progress.toDouble(), mScale)
        } else {
            Math.round(progress).toString()
        }
    }

    private val closestIndex: Int
        get() {
            var closestIndex = 0
            var amplitude = Math.abs(mMax - mMin)
            for (i in mProgressArr.indices) {
                val amplitudeTemp = Math.abs(mProgressArr[i] - mProgress)
                if (amplitudeTemp <= amplitude) {
                    amplitude = amplitudeTemp
                    closestIndex = i
                }
            }
            return closestIndex
        }
    private val amplitude: Float
        get() = if ((mMax - mMin) > 0) (mMax - mMin) else 1f

    private fun setSeekListener(formUser: Boolean) {
        if (mSeekChangeListener == null) {
            return
        }
        if (progressChange()) {
            mSeekChangeListener?.onSeeking(collectParams(formUser))
        }
    }

    private fun collectParams(formUser: Boolean): SeekParams? {
        if (mSeekParams == null) {
            mSeekParams = SeekParams(this)
        }
        mSeekParams!!.progress = progress
        mSeekParams!!.progressFloat = progressFloat
        mSeekParams!!.fromUser = formUser
        //for discrete series seek bar
        if (mTicksCount > 2) {
            val rawThumbPos = thumbPosOnTick
            if (mShowTickText && mTickTextsArr != null) {
                mSeekParams!!.tickText = mTickTextsArr!![rawThumbPos]
            }
            if (mR2L) {
                mSeekParams!!.thumbPosition = mTicksCount - rawThumbPos - 1
            } else {
                mSeekParams!!.thumbPosition = rawThumbPos
            }
        }
        return mSeekParams
    }

    private fun apply(builder: Builder) {
        //seek bar
        mMax = builder.max
        mMin = builder.min
        mProgress = builder.progress
        mIsFloatProgress = builder.progressValueFloat
        mTicksCount = builder.tickCount
        mSeekSmoothly = builder.seekSmoothly
        mIsAboveUpperLimit = builder.isAboveUpperLimit
        mR2L = builder.r2l
        mUserSeekable = builder.userSeekable
        mClearPadding = builder.clearPadding
        mOnlyThumbDraggable = builder.onlyThumbDraggable
        //indicator
        mShowIndicatorType = builder.showIndicatorType
        mIndicatorColor = builder.indicatorColor
        mIndicatorTextColor = builder.indicatorTextColor
        mIndicatorTextSize = builder.indicatorTextSize
        indicatorContentView = builder.indicatorContentView
        mIndicatorTopContentView = builder.indicatorTopContentView
        //track
        mBackgroundTrackSize = builder.trackBackgroundSize
        mBackgroundTrackColor = builder.trackBackgroundColor
        mProgressTrackSize = builder.trackProgressSize
        mProgressTrackColor = builder.trackProgressColor
        mTrackRoundedCorners = builder.trackRoundedCorners
        //thumb
        mThumbSize = builder.thumbSize
        mThumbDrawable = builder.thumbDrawable
        mThumbTextColor = builder.thumbTextColor
        initThumbColor(builder.thumbColorStateList, builder.thumbColor)
        mShowThumbText = builder.showThumbText
        //tickMarks
        mShowTickMarksType = builder.showTickMarksType
        mTickMarksSize = builder.tickMarksSize
        mTickMarksDrawable = builder.tickMarksDrawable
        mTickMarksEndsHide = builder.tickMarksEndsHide
        mTickMarksSweptHide = builder.tickMarksSweptHide
        initTickMarksColor(builder.tickMarksColorStateList, builder.tickMarksColor)
        //tickTexts
        mShowTickText = builder.showTickText
        mTickTextsSize = builder.tickTextsSize
        mTickTextsCustomArray = builder.tickTextsCustomArray
        mTextsTypeface = builder.tickTextsTypeFace
        initTickTextsColor(builder.tickTextsColorStateList, builder.tickTextsColor)
    }

    /**
     * first showing when initial for indicator stay always.
     */
    fun showStayIndicator() {
        indicatorContentView!!.visibility = INVISIBLE
        postDelayed({
            val animation: Animation = AlphaAnimation(0.1f, 1.0f)
            animation.duration = 180
            indicatorContentView!!.animation = animation
            updateStayIndicator()
            indicatorContentView!!.visibility = VISIBLE
        }, 300)
    }

    /**
     * @param indicatorStayAlways IndicatorStayLayout call this, always true.
     */
    fun setIndicatorStayAlways(indicatorStayAlways: Boolean) {
        mIndicatorStayAlways = indicatorStayAlways
    }

    val indicatorTextString: String
        get() {
            if (mIndicatorTextFormat != null && mIndicatorTextFormat!!.contains(FORMAT_TICK_TEXT)) {
                if (mTicksCount > 2 && mTickTextsArr != null) {
                    return mIndicatorTextFormat!!.replace(
                        FORMAT_TICK_TEXT,
                        mTickTextsArr!![thumbPosOnTick]!!
                    )
                }
            } else if (mIndicatorTextFormat != null && mIndicatorTextFormat!!.contains(
                    FORMAT_PROGRESS
                )
            ) {
                return mIndicatorTextFormat!!.replace(FORMAT_PROGRESS, getProgressString(mProgress))
            }
            return getProgressString(mProgress)
        }
    /**
     * get the tick' count
     *
     * @return tick' count
     */
    /**
     * Sets the tick count
     *
     * @param tickCount
     */
    @set:Synchronized
    var tickCount: Int
        get() = mTicksCount
        set(tickCount) {
            require(!(mTicksCount < 0 || mTicksCount > 50)) { "the Argument: TICK COUNT must be limited between (0-50), Now is $mTicksCount" }
            mTicksCount = tickCount
            collectTicksInfo()
            initTextsArray()
            initSeekBarInfo()
            refreshSeekBarLocation()
            invalidate()
            updateStayIndicator()
        }

    /**
     * Get the seek bar's current level of progress in float type.
     *
     * @return current progress in float type.
     */
    @get:Synchronized
    val progressFloat: Float
        get() {
            val bigDecimal = BigDecimal.valueOf(mProgress.toDouble())
            return bigDecimal.setScale(mScale, BigDecimal.ROUND_HALF_UP).toFloat()
        }

    /**
     * Get the seek bar's current level of progress in int type.
     *
     * @return progress in int type.
     */
    val progress: Int
        get() = Math.round(mProgress)
    /**
     * @return the upper limit of this seek bar's range.
     */
    /**
     * Set the upper range of the seek bar
     *
     * @param max the upper range of this progress bar.
     */
    @set:Synchronized
    var max: Float
        get() = mMax
        set(max) {
            mMax = Math.max(mMin, max)
            initProgressRangeValue()
            collectTicksInfo()
            refreshSeekBarLocation()
            invalidate()
            updateStayIndicator()
        }
    /**
     * the lower limit of this seek bar's range.
     *
     * @return the seek bar min value
     */
    /**
     * Set the min value for SeekBar
     *
     * @param min the min value , if is larger than max, will set to max.
     */
    @set:Synchronized
    var min: Float
        get() = mMin
        set(min) {
            mMin = Math.min(mMax, min)
            initProgressRangeValue()
            collectTicksInfo()
            refreshSeekBarLocation()
            invalidate()
            updateStayIndicator()
        }

    @set:Synchronized
    var isAboveUpperLimit: Boolean
        get() = mIsAboveUpperLimit
        set(isAboveUpperLimit) {
            mIsAboveUpperLimit = isAboveUpperLimit
        }

    /**
     * the listener to listen the seeking params changing.
     *
     * @return seeking listener.
     */
    /**
     * Set the listener to listen the seeking params changing.
     *
     * @param listener OnSeekChangeListener
     */
    var onSeekChangeListener: OnSeekChangeListener?
        get() = mSeekChangeListener
        set(listener) {
            mSeekChangeListener = listener
        }

    /**
     * Sets the current progress to the specified value.also,
     * if the seek bar's tick'count is larger than 2,the progress will adjust to the closest tick's progress auto.
     *
     * @param progress a new progress value , if the new progress is less than min ,
     * it will set to min;
     * if over max ,will be max.
     */
    @Synchronized
    fun setProgress(progress: Float) {
        lastProgress = mProgress
        mProgress = if (progress < mMin) mMin else if (progress > mMax) mMax else progress
        //adjust to the closest tick's progress
        if (!mSeekSmoothly && mTicksCount > 2) {
            mProgress = mProgressArr[closestIndex]
        }
        setSeekListener(false)
        refreshThumbCenterXByProgress(mProgress)
        postInvalidate()
        updateStayIndicator()
    }

    /**
     * compat app local change
     *
     * @param isR2L True if see form right to left on the screen.
     */
    fun setR2L(isR2L: Boolean) {
        mR2L = isR2L
        requestLayout()
        invalidate()
        updateStayIndicator()
    }

    /**
     * Set a new thumb drawable.
     *
     * @param drawable the drawable for thumb,selector drawable is ok.
     * selector format:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="@drawable/ic_launcher" android:state_pressed="true" />  <!--this drawable is for thumb when pressing-->
    //<item android:drawable="@drawable/ic_launcher_round" />  < !--for thumb when normal-->
    //</selector>
    fun setThumbDrawable(drawable: Drawable?) {
        if (drawable == null) {
            mThumbDrawable = null
            mThumbBitmap = null
            mPressedThumbBitmap = null
        } else {
            mThumbDrawable = drawable
            mThumbRadius =
                Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH.toFloat()), mThumbSize) / 2.0f
            mThumbTouchRadius = mThumbRadius
            mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f
            initThumbBitmap()
        }
        requestLayout()
        invalidate()
    }

    /**
     * call this will do not draw thumb, true if hide.
     */
    fun hideThumb(hide: Boolean) {
        mHideThumb = hide
        invalidate()
    }

    /**
     * call this will do not draw the text which below thumb. true if hide.
     */
    fun hideThumbText(hide: Boolean) {
        mShowThumbText = !hide
        invalidate()
    }

    /**
     * set the seek bar's thumb's color.
     *
     * @param thumbColor colorInt
     */
    fun thumbColor(@ColorInt thumbColor: Int) {
        mThumbColor = thumbColor
        mPressedThumbColor = thumbColor
        invalidate()
    }

    /**
     * set the seek bar's thumb's selector color.
     *
     * @param thumbColorStateList color selector
     * selector format like:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_pressed="true" />  <!--this color is for thumb which is at pressing status-->
    //<item android:color="@color/color_blue" />                                <!--for thumb which is at normal status-->
    //</selector>
    fun thumbColorStateList(thumbColorStateList: ColorStateList?) {
        initThumbColor(thumbColorStateList, mThumbColor)
        invalidate()
    }

    /**
     * Set a new tick marks drawable.
     *
     * @param drawable the drawable for marks,selector drawable is ok.
     * selector format:
     */
    //< ?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="@drawable/ic_launcher" android:state_selected="true" />  < !--this drawable is for thickMarks which thumb swept-->
    //<item android:drawable="@drawable/ic_launcher_round" />  < !--for thickMarks which thumb haven't reached-->
    //</selector>
    fun setTickMarksDrawable(drawable: Drawable?) {
        if (drawable == null) {
            mTickMarksDrawable = null
            mUnselectTickMarksBitmap = null
            mSelectTickMarksBitmap = null
        } else {
            mTickMarksDrawable = drawable
            mTickRadius =
                Math.min(
                    SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH.toFloat()),
                    mTickMarksSize
                ) / 2.0f
            mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f
            initTickMarksBitmap()
        }
        invalidate()
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColor colorInt
     */
    fun tickMarksColor(@ColorInt tickMarksColor: Int) {
        mSelectedTickMarksColor = tickMarksColor
        mUnSelectedTickMarksColor = tickMarksColor
        invalidate()
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColorStateList colorInt
     * selector format like:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_selected="true" />  <!--this color is for marks those are at left side of thumb-->
    //<item android:color="@color/color_gray" />                                 <!--for marks those are at right side of thumb-->
    //</selector>
    fun tickMarksColor(tickMarksColorStateList: ColorStateList?) {
        initTickMarksColor(tickMarksColorStateList, mSelectedTickMarksColor)
        invalidate()
    }

    /**
     * set the color for text below/above seek bar's tickText.
     *
     * @param tickTextsColor ColorInt
     */
    fun tickTextsColor(@ColorInt tickTextsColor: Int) {
        mUnselectedTextsColor = tickTextsColor
        mSelectedTextsColor = tickTextsColor
        mHoveredTextColor = tickTextsColor
        invalidate()
    }

    /**
     * set the selector color for text below/above seek bar's tickText.
     *
     * @param tickTextsColorStateList ColorInt
     * selector format like:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_selected="true" />  <!--this color is for texts those are at left side of thumb-->
    //<item android:color="@color/color_blue" android:state_hovered="true" />     <!--for thumb below text-->
    //<item android:color="@color/color_gray" />                                 <!--for texts those are at right side of thumb-->
    //</selector>
    fun tickTextsColorStateList(tickTextsColorStateList: ColorStateList?) {
        initTickTextsColor(tickTextsColorStateList, mSelectedTextsColor)
        invalidate()
    }

    /**
     * The specified scale for the progress value,
     * make sure you had chosen the float progress type
     *
     *
     * such as:
     * scale = 3; progress: 1.78627347 to 1.786
     * scale = 4; progress: 1.78627347 to 1.7863
     *
     *
     * make sure you have call the attr progress_value_float=true before, otherwise no change.
     *
     * @param scale scale for the float type progress value.
     */
    fun setDecimalScale(scale: Int) {
        mScale = scale
    }

    /**
     * Set a format string with placeholder ${PROGRESS} or ${TICK_TEXT} to IndicatorSeekBar,
     * the indicator's text would change.
     * For example:
     * seekBar.setIndicatorTextFormat("${PROGRESS} %");
     * seekBar.setIndicatorTextFormat("${PROGRESS} miles");
     * seekBar.setIndicatorTextFormat("I am ${TICK_TEXT}%");
     *
     *
     * make sure you have custom and show the tick text before you
     * use ${TICK_TEXT}% , otherwise will be shown a "" value.
     *
     *
     * Also, if the SeekBar type is Custom ,this method will be no work, see[IndicatorType]
     *
     * @param format the format for indicator text
     */
    fun setIndicatorTextFormat(format: String?) {
        mIndicatorTextFormat = format
        initTextsArray()
        updateStayIndicator()
    }

    /**
     * Collect and custom the color for each of section track.
     *
     *
     * usage :
     *
     *
     * indicatorSeekBar.customSectionTrackColor(new ColorCollector() {
     *
     *
     * public boolean collectSectionTrackColor(int[] colorIntArr) {
     * colorIntArr[0] = getResources().getColor(R.color.color_blue);
     * colorIntArr[1] = getResources().getColor(R.color.color_gray);
     * colorIntArr[2] = Color.parseColor("#FFFF00");
     * ......
     * return true; // True if apply color , otherwise no change.
     * }
     * });
     *
     * @param collector The container for section track's color
     */
    fun customSectionTrackColor(@NonNull collector: ColorCollector) {
        val colorArray = IntArray(if (mTicksCount - 1 > 0) mTicksCount - 1 else 1)
        for (i in colorArray.indices) {
            //set the default section color
            colorArray[i] = mBackgroundTrackColor
        }
        mCustomTrackSectionColorResult = collector.collectSectionTrackColor(colorArray)
        mSectionTrackColorArray = colorArray
        invalidate()
    }

    /**
     * Replace the number ticks' texts with your's by String[].
     * Usually, the text array's length your set should equals seek bar's tickMarks' count.
     *
     * @param tickTextsArr The array contains the tick text
     */
    fun customTickTexts(@NonNull tickTextsArr: Array<CharSequence>) {
        mTickTextsCustomArray = tickTextsArr
        if (mTickTextsArr != null) {
            for (i in mTickTextsArr!!.indices) {
                var tickText: String
                tickText = if (i < tickTextsArr.size) {
                    tickTextsArr.get(i).toString()
                } else {
                    ""
                }
                var index = i
                if (mR2L) {
                    index = mTicksCount - 1 - i
                }
                mTickTextsArr!![index] = tickText
                if (mTextPaint != null && mRect != null) {
                    mTextPaint!!.getTextBounds(tickText, 0, tickText.length, mRect)
                    mTickTextsWidth[index] = mRect!!.width().toFloat()
                }
            }
            invalidate()
        }
    }

    /**
     * Set the custom tick texts' typeface you want.
     *
     * @param typeface The typeface for tickTexts.
     */
    fun customTickTextsTypeface(typeface: Typeface?) {
        mTextsTypeface = typeface
        measureTickTextsBonds()
        requestLayout()
        invalidate()
    }

    /**
     * only show the tick texts on both of ends seek bar, make sure you hava called the attr:show tick text before.
     *
     * @param onlyShow true if only show the tick texts on both of ends seek bar
     */
    fun showBothEndsTickTextsOnly(onlyShow: Boolean) {
        mShowBothTickTextsOnly = onlyShow
    }

    /**
     * prevent user from seeking
     *
     * @param seekAble true if user can seek
     */
    fun setUserSeekAble(seekAble: Boolean) {
        mUserSeekable = seekAble
    }

    /**
     * Sets the thumb move to the closed tick after touched up automatically, default true
     *
     * @param adjustAuto true if auto move after touched up.
     */
    fun setThumbAdjustAuto(adjustAuto: Boolean) {
        mAdjustAuto = adjustAuto
    } /*------------------API END-------------------*/

    companion object {
        private const val THUMB_MAX_WIDTH = 30
        private const val FORMAT_PROGRESS = "\${PROGRESS}"
        private const val FORMAT_TICK_TEXT = "\${TICK_TEXT}"
        /*------------------API START-------------------*/
        /**
         * call this to new a builder with default params.
         *
         * @param context context environment
         * @return Builder
         */
        fun with(context: Context?): Builder {
            return Builder(context!!)
        }
    }
}