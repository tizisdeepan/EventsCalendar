package com.event.calendar.eventcalendar.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.event.calendar.eventcalendar.R
import com.event.calendar.eventcalendar.utils.MeasureUtils
import com.event.calendar.eventcalendar.utils.ZMailCalendarUtil
import java.util.*

class DateTextView : View {

    var mDateSelectListener: DateSelectListener? = null
    private var mDate: Calendar? = null
    private var mDateTextSize: Float = 0.toFloat()
    private var mDotRadius: Float = 0.toFloat()
    private var mDotX: Int = 0
    private var mDotY: Int = 0
    private var mCircleX: Int = 0
    private var mCircleY: Int = 0
    private var mDateTextX: Int = 0
    private var mDateTextY: Float = 0.toFloat()
    private var mTodayCircleradius: Float = 0.toFloat()

    private lateinit var mContext: Context
    private var mAttrs: AttributeSet? = null
    private var mDefStyleAttr: Int = 0
    private var mDefStyleRes: Int = 0

    internal var isCurrentMonth: Boolean = false
    internal var hasEvent: Boolean = false
    internal var isSelected: Boolean = false
    internal var isToday: Boolean = false
    internal var isPast: Boolean = false
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mBgCircleRadius: Float = 0.toFloat()

    private var animate: Boolean = false    //Setting animate to 'true' will trigger repeated 'invalidate()' based on 'frameCount' resulting in Animation of SELECTION CIRCLE
    private var frameCount: Int = 0

    private var touchDown = false
    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()

    var date: Calendar
        get() = mDate!!.clone() as Calendar
        set(date) {
            mDate = date.clone() as Calendar
            invalidate()
        }

    interface DateSelectListener {
        fun onDateTextViewSelected(dateTextView: DateTextView, isClick: Boolean)
    }

    constructor(context: Context) : super(context) {
        init(context, null, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, -1)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        //INITIALIZATION TASKS
        mContext = context
        mAttrs = attrs
        mDefStyleAttr = defStyleAttr
        mDefStyleRes = defStyleRes

        this.isClickable = true

        mDate = Calendar.getInstance()
        mDotRadius = resources.getDimension(R.dimen.radius_event_dot)

        val attributes = mContext.theme.obtainStyledAttributes(attrs, R.styleable.DateTextView, defStyleAttr, defStyleRes)
        try {
            isCurrentMonth = attributes.getBoolean(R.styleable.DateTextView_isCurrentMonth, false)
            isSelected = attributes.getBoolean(R.styleable.DateTextView_isSelected, false)
            hasEvent = attributes.getBoolean(R.styleable.DateTextView_hasEvent, false)
            isToday = attributes.getBoolean(R.styleable.DateTextView_isToday, false)
            isPast = attributes.getBoolean(R.styleable.DateTextView_isPast, false)
        } finally {
            attributes.recycle()
        }

        if (doInitializeStaticVariables) {
            selectedTextColor = ZMailCalendarUtil.selectedTextColor
            selectionCircleColor = ZMailCalendarUtil.selectionColor
            defaultTextColor = ZMailCalendarUtil.primaryTextColor
            disabledTextColor = ZMailCalendarUtil.secondaryTextColor
            eventDotColor = ZMailCalendarUtil.eventDotColor
            datesTypeface = ZMailCalendarUtil.datesTypeface

            mSelectionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mSelectionPaint?.color = selectionCircleColor

            mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mDotPaint?.color = eventDotColor

            mTodayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mTodayPaint?.color = context.resources.getColor(R.color.colorPrimary)
            mTodayPaint?.style = Paint.Style.STROKE
            mTodayPaint?.strokeWidth = resources.getDimension(R.dimen.width_circle_stroke)


            mDateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mDateTextPaint?.textAlign = Paint.Align.CENTER
            mDateTextPaint?.color = ZMailCalendarUtil.primaryTextColor
            mDateTextSize = mContext.resources.getDimension(R.dimen.text_calendar_date)
            mDateTextPaint?.textSize = mDateTextSize

            doInitializeStaticVariables = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureUtils.getMeasurement(widthMeasureSpec, mContext.resources.getDimension(R.dimen.dimen_date_text_view).toInt())
        val height = MeasureUtils.getMeasurement(heightMeasureSpec, mContext.resources.getDimension(R.dimen.dimen_date_text_view).toInt())
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        mDateTextSize = height * (3.2f / 10f)
        mDateTextPaint!!.textSize = mDateTextSize
        mDateTextX = width / 2
        mDateTextY = height / 2 - (mDateTextPaint!!.ascent() + mDateTextPaint!!.descent()) / 2

        mBgCircleRadius = Math.min(height - height * (6f / 10f), width - width * (6f / 10f))
        mTodayCircleradius = mBgCircleRadius - resources.getDimension(R.dimen.width_circle_stroke) / 2
        mCircleX = width / 2
        mCircleY = height / 2

        mDotRadius = height * (0.75f / 20f)
        mDotX = width / 2
        mDotY = (height * (7.5f / 10f)).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        val location = IntArray(2)
        this.getLocationOnScreen(location)

        if (datesTypeface != null) mDateTextPaint?.typeface = datesTypeface
        mDateTextPaint?.isFakeBoldText = false

        if (isPast) {
            mDateTextPaint!!.color = disabledTextColor
            canvas.drawText("" + mDate!!.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint!!)
        } else {
            if (isCurrentMonth) {
                if (isToday) {
                    canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mTodayCircleradius, mTodayPaint!!)
                }
                if (isSelected) {
                    mDateTextPaint?.color = selectedTextColor
                    if (ZMailCalendarUtil.isBoldTextOnSelectionEnabled) mDateTextPaint?.isFakeBoldText = true
                    //DRAWING SELECTION CIRCLE
                    //EDITED --> if(animate)
                    //                if (false)
                    //                {
                    //                    canvas.drawCircle(mCircleX, mCircleY, mBgCircleRadius * (frameCount / 10f), mSelectionPaint);
                    //                }
                    //                else
                    //                {
                    canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mBgCircleRadius, mSelectionPaint!!)
                    //                }
                } else {
                    mDateTextPaint?.color = defaultTextColor
                    //EDITED --> if(animate)
                    //                if (false)
                    //                {
                    //                    REMOVING SELECTION CIRCLE
                    //                    canvas.drawCircle(mCircleX, mCircleY, mBgCircleRadius * (1 - (frameCount / 10f)), mSelectionPaint);
                    //                }
                }
                canvas.drawText("" + mDate!!.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint!!)
            } else {
                mDateTextPaint?.color = disabledTextColor
                canvas.drawText("" + mDate!!.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint!!)
            }
        }

        super.onDraw(canvas)

        if (hasEvent) drawDot(canvas)

        if (animate) {
            frameCount++
            if (frameCount == 10) {
                animate = false
                frameCount = 0
            }
            invalidate()
        }
    }

    /**
     * Draws "Event Dot"
     */
    private fun drawDot(canvas: Canvas) {
        if (isCurrentMonth) {
            if (isSelected) {
                mDotPaint?.color = selectedTextColor
                canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint!!)
                mDotPaint?.color = ZMailCalendarUtil.selectionColor
            } else {
                mDotPaint?.color = eventDotColor
                canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint!!)
            }
        } else {
            mDotPaint?.color = disabledTextColor
            canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint!!)
            mDotPaint?.color = ZMailCalendarUtil.selectionColor
        }
    }

    /**
     * Set properties of DateTextView . Usually used when adding this view manually .
     */
    fun setProperties(isCurrentMonth: Boolean, hasEvent: Boolean, isSelected: Boolean, isToday: Boolean, date: Calendar, isPast: Boolean) {
        this.isCurrentMonth = isCurrentMonth
        this.hasEvent = hasEvent
        this.isSelected = isSelected
        this.isToday = isToday
        this.isPast = isPast
        mDate = date.clone() as Calendar

        invalidate()
    }

    fun setDateClickListener(dateSelectListener: DateSelectListener) {
        mDateSelectListener = dateSelectListener
    }

    fun setIsCurrentMonth(isCurrentMonth: Boolean) {
        this.isCurrentMonth = isCurrentMonth
        invalidate()
    }

    fun setHasEvent(hasEvent: Boolean) {
        this.hasEvent = hasEvent
        invalidate()
    }

    fun setIsSelected(isSelected: Boolean) {
        this.isSelected = isSelected
        invalidate()
    }

    fun setIsToday(isToday: Boolean) {
        this.isToday = isToday
        invalidate()
    }

    fun setIsPast(isPast: Boolean) {
        this.isPast = isPast
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDown = true
                mDownX = event.x
                mDownY = event.y
            }
            MotionEvent.ACTION_UP -> if (touchDown && isPointerInsideArea(event)) if (isCurrentMonth) select(true) else select(false)
        }
        return super.onTouchEvent(event)
    }

    /**
     * @return true if MotionEvent Pointer is within bounds of this view
     */
    private fun isPointerInsideArea(event: MotionEvent): Boolean {
        touchDown = false
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)
        val leftLimit = locationOnScreen[0]
        val upperLimit = locationOnScreen[1]
        val rightLimit = leftLimit + measuredWidth
        val lowerLimit = upperLimit + measuredHeight
        val x = event.x + locationOnScreen[0]
        val y = event.y + locationOnScreen[1]
        return x > leftLimit && x < rightLimit && y > upperLimit && y < lowerLimit
    }

    /**
     * starts "growing circle animation"
     */
    private fun startSelectedAnimation() {
        animate = true
        frameCount = 0
        setIsSelected(true)
    }

    /**
     * starts "shrinking circle animation"
     */
    private fun startUnselectedAnimation() {
        animate = true
        frameCount = 0
        setIsSelected(false)
    }

    /**
     * Called to select this DateTextView
     *
     * @param isClick true if selection is triggered by Click (explicit touch event)
     */
    fun select(isClick: Boolean) {
        if (!isSelected && mDateSelectListener != null) {
            if (isClick) startSelectedAnimation() else setIsSelected(true)
            mDateSelectListener?.onDateTextViewSelected(this, isClick)
        }
    }

    fun selectOnPageChange(isClick: Boolean) {
        if (!isSelected && mDateSelectListener != null) {
            setIsSelected(false)
            mDateSelectListener?.onDateTextViewSelected(this, false)
        }
    }

    /**
     * Called to un-select this DateTextView
     *
     * @param isClick true if un-selection is triggered by Click (explicit touch event)
     */
    fun unSelect(isClick: Boolean) {
        if (isClick && isCurrentMonth) startUnselectedAnimation() else setIsSelected(false)
    }

    companion object {
        private var doInitializeStaticVariables = true
        private var mDotPaint: Paint? = null
        private var mSelectionPaint: Paint? = null
        private var mTodayPaint: Paint? = null
        private var mDateTextPaint: Paint? = null
        private var selectedTextColor: Int = 0
        private var datesTypeface: Typeface? = null
        private var defaultTextColor: Int = 0
        private var disabledTextColor: Int = 0
        private var selectionCircleColor: Int = 0
        private var eventDotColor: Int = 0
        fun invalidateColors() {
            doInitializeStaticVariables = true
        }
    }
}
