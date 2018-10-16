package com.events.calendar.views

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.events.calendar.R
import com.events.calendar.utils.EventsCalendarUtil
import java.util.*

@Suppress("NAME_SHADOWING")
class DateText : View {

    private lateinit var mDateSelectListener: DateSelectListener
    private lateinit var mDate: Calendar
    private var mDateTextSize = 0f
    private var mDotRadius = 0f
    private var mDotX = 0
    private var mDotY = 0
    private var mCircleX = 0
    private var mCircleY = 0
    private var mDateTextX = 0
    private var mDateTextY = 0f
    private var mTodayCircleRadius = 0f
    private lateinit var mContext: Context
    private var mAttrs: AttributeSet? = null
    private var mDefStyleAttr = 0
    private var mDefStyleRes = 0
    internal var isCurrentMonth = false
    internal var hasEvent = false
    internal var isSelected = false
    internal var isToday = false
    internal var isPast = false
    private var mWidth = 0
    private var mHeight = 0
    private var mBgCircleRadius = 0f
    private var mFullCircleRadius = 0f
    private var isDisabled = false
    private var touchDown = false
    private var mDownX = 0f
    private var mDownY = 0f

    private val listenerAdapter = InternalListener()

    var date: Calendar
        get() = mDate.clone() as Calendar
        set(date) {
            mDate = date.clone() as Calendar
            invalidate()
        }

    interface DateSelectListener {
        fun onDateTextViewSelected(dateText: DateText, isClick: Boolean)
        fun onDateTextViewLongSelected(dateText: DateText, isClick: Boolean)
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
        mContext = context
        mAttrs = attrs
        mDefStyleAttr = defStyleAttr
        mDefStyleRes = defStyleRes
        this.isClickable = true
        mDate = Calendar.getInstance()
        mDotRadius = resources.getDimension(R.dimen.radius_event_dot)

        val attributes = mContext.theme.obtainStyledAttributes(attrs, R.styleable.DateText, defStyleAttr, defStyleRes)
        try {
            isCurrentMonth = attributes.getBoolean(R.styleable.DateText_isCurrentMonth, false)
            isSelected = attributes.getBoolean(R.styleable.DateText_isSelected, false)
            hasEvent = attributes.getBoolean(R.styleable.DateText_hasEvent, false)
            isToday = attributes.getBoolean(R.styleable.DateText_isToday, false)
            isPast = attributes.getBoolean(R.styleable.DateText_isPast, false)
        } finally {
            attributes.recycle()
        }

        if (doInitializeStaticVariables) {
            selectedTextColor = EventsCalendarUtil.selectedTextColor
            selectionCircleColor = EventsCalendarUtil.selectionColor
            defaultTextColor = EventsCalendarUtil.primaryTextColor
            disabledTextColor = EventsCalendarUtil.secondaryTextColor
            eventDotColor = EventsCalendarUtil.eventDotColor
            rangeSelectionColor = EventsCalendarUtil.rangeSelectionColor
            rangeSelectionStartColor = EventsCalendarUtil.rangeSelectionStartColor
            rangeSelectionEndColor = EventsCalendarUtil.rangeSelectionEndColor

            mSelectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = selectionCircleColor
            }

            mRangeSelectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = rangeSelectionColor
            }

            mRangeSelectionStartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = rangeSelectionStartColor
            }

            mRangeSelectionEndPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = rangeSelectionEndColor
            }

            mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = eventDotColor
            }

            mTodayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = EventsCalendarUtil.primaryTextColor
                style = Paint.Style.STROKE
                strokeWidth = resources.getDimension(R.dimen.width_circle_stroke)
            }

            mDateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                color = EventsCalendarUtil.primaryTextColor
                mDateTextSize = mContext.resources.getDimension(R.dimen.text_calendar_date)
                textSize = mDateTextSize
            }

            doInitializeStaticVariables = false
        }

        listenerAdapter.setCustomLongClickListener(OnLongClickListener {
            mDateSelectListener.onDateTextViewLongSelected(this@DateText, true)
            true
        })

        setOnLongClickListener(listenerAdapter.longClickListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasurement(widthMeasureSpec, mContext.resources.getDimension(R.dimen.dimen_date_text_view).toInt())
        val height = getMeasurement(heightMeasureSpec, mContext.resources.getDimension(R.dimen.dimen_date_text_view).toInt())
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        mDateTextSize = mHeight * (3.2f / 10f)
        mDateTextPaint.textSize = mDateTextSize
        mDateTextX = mWidth / 2
        mDateTextY = mHeight / 2 - (mDateTextPaint.ascent() + mDateTextPaint.descent()) / 2

        mBgCircleRadius = Math.min(mHeight - mHeight * 0.6f, mWidth - mWidth * 0.6f)
        mFullCircleRadius = Math.min(mHeight - mHeight * 0.5f, mWidth - mWidth * 0.5f)
        mTodayCircleRadius = mBgCircleRadius - resources.getDimension(R.dimen.width_circle_stroke) / 2
        mCircleX = mWidth / 2
        mCircleY = mHeight / 2

        mDotRadius = mHeight * (0.75f / 20f)
        mDotX = mWidth / 2
        mDotY = (mHeight * (7.5f / 10f)).toInt()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        if (isPast) {
            mDateTextPaint.color = disabledTextColor
            canvas.drawText("" + mDate.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint)
        } else {
            if (isCurrentMonth && !isDisabled) {
                if (isToday) canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mTodayCircleRadius, mTodayPaint)
                when (EventsCalendarUtil.SELECTION_MODE) {
                    EventsCalendarUtil.SINGLE_SELECTION -> {
                        if (isSelected) {
                            mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                            mDateTextPaint.color = selectedTextColor
                            canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mBgCircleRadius, mSelectionPaint)
                        } else mDateTextPaint.color = defaultTextColor
                    }
                    EventsCalendarUtil.RANGE_SELECTION -> {
                        when {
                            EventsCalendarUtil.datesInSelectedRange.contains(EventsCalendarUtil.getDateString(mDate, EventsCalendarUtil.DD_MM_YYYY)) -> when {
                                EventsCalendarUtil.datesInSelectedRange.keys.indexOf(EventsCalendarUtil.getDateString(mDate, EventsCalendarUtil.DD_MM_YYYY)) == 0 -> {
                                    mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                                    mDateTextPaint.color = selectedTextColor
                                    canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mFullCircleRadius, mRangeSelectionStartPaint)
                                    canvas.drawRect(RectF((mWidth / 2).toFloat(), 0f, mWidth.toFloat(), mHeight.toFloat()), mRangeSelectionStartPaint)
                                }
                                EventsCalendarUtil.datesInSelectedRange.keys.indexOf(EventsCalendarUtil.getDateString(mDate, EventsCalendarUtil.DD_MM_YYYY)) == EventsCalendarUtil.datesInSelectedRange.size - 1 -> {
                                    mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                                    mDateTextPaint.color = selectedTextColor
                                    canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mFullCircleRadius, mRangeSelectionEndPaint)
                                    canvas.drawRect(RectF(0f, 0f, (mWidth / 2).toFloat(), mHeight.toFloat()), mRangeSelectionEndPaint)
                                }
                                else -> {
                                    mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                                    mDateTextPaint.color = selectedTextColor
                                    canvas.drawColor(mRangeSelectionPaint.color)
                                }
                            }
                            isSelected -> {
                                mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                                mDateTextPaint.color = selectedTextColor
                                canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mBgCircleRadius, mSelectionPaint)
                            }
                            else -> mDateTextPaint.color = defaultTextColor
                        }
                    }
                    EventsCalendarUtil.MULTIPLE_SELECTION -> {
                        if (EventsCalendarUtil.datesInSelectedRange.contains(EventsCalendarUtil.getDateString(mDate, EventsCalendarUtil.DD_MM_YYYY))) {
                            mDateTextPaint.isFakeBoldText = EventsCalendarUtil.isBoldTextOnSelectionEnabled
                            mDateTextPaint.color = selectedTextColor
                            canvas.drawCircle(mCircleX.toFloat(), mCircleY.toFloat(), mBgCircleRadius, mSelectionPaint)
                        } else mDateTextPaint.color = defaultTextColor
                    }
                }
                canvas.drawText("" + mDate.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint)
            } else {
                mDateTextPaint.color = disabledTextColor
                canvas.drawText("" + mDate.get(Calendar.DATE), mDateTextX.toFloat(), mDateTextY, mDateTextPaint)
            }
        }
        super.onDraw(canvas)
        if (hasEvent) {
            drawDot(canvas)
        }
    }

    private fun drawDot(canvas: Canvas) {
        if (isCurrentMonth && !isDisabled) {
            if (isSelected || EventsCalendarUtil.datesInSelectedRange.contains(EventsCalendarUtil.getDateString(mDate, EventsCalendarUtil.DD_MM_YYYY))) {
                mDotPaint.color = selectedTextColor
                canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint)
                mDotPaint.color = EventsCalendarUtil.selectionColor
            } else {
                mDotPaint.color = eventDotColor
                canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint)
            }
        } else {
            mDotPaint.color = disabledTextColor
            canvas.drawCircle(mDotX.toFloat(), mDotY.toFloat(), mDotRadius, mDotPaint)
            mDotPaint.color = EventsCalendarUtil.selectionColor
        }
    }

    fun setProperties(isCurrentMonth: Boolean, hasEvent: Boolean, isSelected: Boolean, isToday: Boolean, date: Calendar, isPast: Boolean, isDisabled: Boolean) {
        this.isCurrentMonth = isCurrentMonth
        this.hasEvent = hasEvent
        this.isSelected = isSelected
        this.isToday = isToday
        this.isPast = isPast
        this.isDisabled = isDisabled
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

    @SuppressLint("ClickableViewAccessibility")
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

    private fun selectAction() {
        setIsSelected(true)
    }

    private fun unselectAction() {
        setIsSelected(false)
    }

    fun select(isClick: Boolean) {
        if (!isSelected && !isDisabled) {
            if (isClick) selectAction() else setIsSelected(true)
            mDateSelectListener.onDateTextViewSelected(this, isClick)
        }
    }

    fun selectOnPageChange(isClick: Boolean) {
        val isClick = false
        if (!isSelected && !isDisabled) {
            setIsSelected(false)
            mDateSelectListener.onDateTextViewSelected(this, isClick)
        }
    }

    fun unSelect(isClick: Boolean) {
        if (isClick && isCurrentMonth && !isDisabled) unselectAction() else setIsSelected(false)
    }

    private fun getMeasurement(measureSpec: Int, contentSize: Int): Int {
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            View.MeasureSpec.UNSPECIFIED -> contentSize
            View.MeasureSpec.AT_MOST -> Math.min(specSize, contentSize)
            View.MeasureSpec.EXACTLY -> specSize
            else -> 0
        }
    }

    companion object {
        private var doInitializeStaticVariables = true
        private lateinit var mDotPaint: Paint
        private lateinit var mSelectionPaint: Paint
        private lateinit var mRangeSelectionPaint: Paint
        private lateinit var mRangeSelectionStartPaint: Paint
        private lateinit var mRangeSelectionEndPaint: Paint
        private lateinit var mTodayPaint: Paint
        private lateinit var mDateTextPaint: Paint
        private var selectedTextColor = 0
        private var defaultTextColor = 0
        private var disabledTextColor = 0
        private var selectionCircleColor = 0
        private var rangeSelectionColor = 0
        private var rangeSelectionStartColor = 0
        private var rangeSelectionEndColor = 0
        private var eventDotColor = 0
        fun invalidateColors() {
            doInitializeStaticVariables = true
        }
    }

    private class InternalListener : View.OnLongClickListener {
        var longClickListener: OnLongClickListener? = null

        fun setCustomLongClickListener(newListener: OnLongClickListener) {
            longClickListener = newListener
        }

        override fun onLongClick(p0: View?): Boolean {
            longClickListener?.onLongClick(p0)
            return true
        }

    }
}
