package com.events.calendar.views

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.AttributeSet
import android.util.MonthDisplayHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.events.calendar.R
import com.events.calendar.utils.EventDots
import com.events.calendar.utils.ZEvents
import com.events.calendar.utils.ZMailCalendarUtil
import java.util.*

open class MonthDatesGridLayout : ViewGroup, DateTextView.DateSelectListener {

    lateinit var mContext: Context
    lateinit var mLayoutInflater: LayoutInflater
    private var mAttrs: AttributeSet? = null
    private var mDefStyleAttr: Int = 0
    private var mDefStyleRes: Int = 0

    private var mMonth: Int = 0
    private var mYear: Int = 0
    private var mSelectedWeekNo: Int = 0
    lateinit var mCurrentCalendar: Calendar
    lateinit var mMonthDisplayHelper: MonthDisplayHelper
    private var mNoOfWeeks: Int = 0
    private var mNoOfCurrentMonthDays: Int = 0
    private var mMonthStartDayOffset: Int = 0
    private var mTotalNoOfDays: Int = 0

    private var mDateTextWidth: Float = 0.toFloat()
    private var mDateTextHeight: Float = 0.toFloat()
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var isLoadingFirstTime = true
    private var mTranslationDistance: Float = 0.toFloat()
    var mCallback: CallBack? = null
    private var mSelectedDatePosition: Int = 0
    private var mPreviousMonthDots: EventDots? = null
    private var mCurrentMonthData: EventDots? = null
    private var mNextMonthDots: EventDots? = null
    private var mDotsInclusionArray: ArrayList<DateTextView>? = null
    private var mDotsRemovalArray: ArrayList<DateTextView>? = null
    private var mRefreshDotsTask: AsyncTask<Int, Int, Boolean>? = null
    private var mResetTaskExecuting: Boolean = false

    interface CallBack {
        fun onDaySelected(date: Calendar?, isClick: Boolean)
    }

    constructor(context: Context) : super(context) {
        init(context, null, -1, -1, true)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, -1, -1, true)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, -1, true)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes, true)
    }

    constructor(context: Context, month: Int, year: Int, weekStartDay: Int, selectedWeekNo: Int) : super(context) {
        mMonth = month
        mYear = year
        mSelectedWeekNo = selectedWeekNo
        sWeekStartDay = weekStartDay
        init(context, null, -1, -1, false)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, doGetAttributes: Boolean) {
        mContext = context
        mAttrs = attrs
        mDefStyleAttr = defStyleAttr
        mDefStyleRes = defStyleRes
        mLayoutInflater = LayoutInflater.from(mContext)

        isClickable = true

        mCurrentCalendar = Calendar.getInstance()
        mCurrentCalendar.set(Calendar.DATE, 1)

        if (doGetAttributes) getAttributeValues()

        setCalendarProperties()
        getDotsData()
        addChildViews()
    }

    private fun getAttributeValues() {
        mMonth = mCurrentCalendar.get(Calendar.MONTH)
        mYear = mCurrentCalendar.get(Calendar.YEAR)

        val attributes = mContext.theme.obtainStyledAttributes(mAttrs, R.styleable.MonthDatesGridLayout, mDefStyleAttr, mDefStyleRes)
        try {
            mMonth = attributes.getInt(R.styleable.MonthDatesGridLayout_month, mMonth)
            mYear = attributes.getInt(R.styleable.MonthDatesGridLayout_year, mYear)
            sWeekStartDay = attributes.getInt(R.styleable.MonthDatesGridLayout_weekStartDay, Calendar.MONDAY)
            mSelectedWeekNo = attributes.getInt(R.styleable.MonthDatesGridLayout_selectedWeekNo, 1)
        } finally {
            attributes.recycle()
        }
    }

    private fun setCalendarProperties() {
        mCurrentCalendar.set(Calendar.MONTH, mMonth)
        mCurrentCalendar.set(Calendar.YEAR, mYear)
        mMonthDisplayHelper = MonthDisplayHelper(mYear, mMonth, sWeekStartDay)
        mNoOfCurrentMonthDays = mMonthDisplayHelper.numberOfDaysInMonth
        mMonthStartDayOffset = mMonthDisplayHelper.offset
        mNoOfWeeks = if (showOnlyCurrentMonthWeeks) Math.ceil(((mNoOfCurrentMonthDays + mMonthStartDayOffset).toFloat() / 7.0f).toDouble()).toInt() else 6
        mTotalNoOfDays = 7 * mNoOfWeeks
        mTranslationDistance = (mSelectedWeekNo - 1) * mContext.resources.getDimension(R.dimen.dimen_date_text_view)
        translationY = -mTranslationDistance
    }

    private fun getDotsData() {
        val iterator = mCurrentCalendar.clone() as Calendar
        iterator.add(Calendar.MONTH, -1)
        mPreviousMonthDots = ZEvents.getDotsForMonth(iterator)
        iterator.add(Calendar.MONTH, 1)
        mCurrentMonthData = ZEvents.getDotsForMonth(iterator)
        iterator.add(Calendar.MONTH, 1)
        mNextMonthDots = ZEvents.getDotsForMonth(iterator)
    }

    private fun addChildViews() {
        var isCurrentMonth: Boolean
        var hasEvent: Boolean
        var isPast: Boolean
        var isSelected = false

        mCurrentCalendar.add(Calendar.DATE, -mMonthStartDayOffset)
        var dotsData: EventDots?
        for (i in 1..mTotalNoOfDays) {
            val dateTextView = DateTextView(mContext)
            dateTextView.setDateClickListener(this)

            if (i < mMonthStartDayOffset) {
                isCurrentMonth = false
                dotsData = mPreviousMonthDots
            } else if (i > mMonthStartDayOffset && i < mNoOfCurrentMonthDays + mMonthStartDayOffset + 1) {
                isCurrentMonth = true
                dotsData = mCurrentMonthData
            } else {
                isCurrentMonth = false
                dotsData = mNextMonthDots
            }

            hasEvent = dotsData != null && dotsData.hasEvent(mCurrentCalendar.get(Calendar.DATE))

            isPast = mCurrentCalendar.timeInMillis <= Calendar.getInstance().timeInMillis - 86400000

            dateTextView.setProperties(isCurrentMonth, hasEvent, isSelected, ZMailCalendarUtil.isToday(mCurrentCalendar), mCurrentCalendar, isPast)

            if (!ZEvents.isWithinMonthSpan(mCurrentCalendar)) dateTextView.isClickable = false

            if (ZMailCalendarUtil.areDatesSame(ZMailCalendarUtil.getCurrentSelectedDate(), mCurrentCalendar) && isLoadingFirstTime && sSelectedDateTextView == null) {
                sSelectedDateTextView = dateTextView
                mSelectedDatePosition = i - 1
                dateTextView.select(false)
            }

            addView(dateTextView)
            mCurrentCalendar.add(Calendar.DATE, 1)
            isSelected = false
        }
        mCurrentCalendar.set(Calendar.DATE, 1)
        mCurrentCalendar.set(Calendar.MONTH, mMonth)
        mCurrentCalendar.set(Calendar.YEAR, mYear)
    }

    fun refreshDots() {
        if (mResetTaskExecuting) {
            mRefreshDotsTask?.cancel(true)
        }
        mRefreshDotsTask = RefreshDotsTask()
        mRefreshDotsTask?.execute()
    }

    fun refreshToday() {
        for (i in 1..mTotalNoOfDays) {
            val dateTextView = getChildAt(i - 1) as DateTextView
            dateTextView.setIsToday(false)
            val today = i - mMonthStartDayOffset
            if (today == ZMailCalendarUtil.today.get(Calendar.DATE)) {
                dateTextView.setIsToday(true)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class RefreshDotsTask : AsyncTask<Int, Int, Boolean>() {

        override fun doInBackground(vararg p0: Int?): Boolean {
            mResetTaskExecuting = true
            if (mDotsInclusionArray == null) mDotsInclusionArray = ArrayList()
            mDotsInclusionArray?.clear()
            if (mDotsRemovalArray == null) mDotsRemovalArray = ArrayList()
            mDotsRemovalArray?.clear()
            fillDotsModificationArrays()
            return false
        }

        private fun fillDotsModificationArrays() {
            mCurrentCalendar.add(Calendar.DATE, -mMonthStartDayOffset)
            var dotsData: EventDots?
            for (i in 1..mTotalNoOfDays) {
                dotsData = if (i < mMonthStartDayOffset) mPreviousMonthDots
                else if (i > mMonthStartDayOffset && i < mNoOfCurrentMonthDays + mMonthStartDayOffset + 1) mCurrentMonthData
                else mNextMonthDots
                val dateTextView = getChildAt(i - 1) as DateTextView
                if (dotsData != null) {
                    if (dateTextView.hasEvent && !dotsData.hasEvent(mCurrentCalendar.get(Calendar.DATE))) mDotsRemovalArray?.add(dateTextView)
                    else if (!dateTextView.hasEvent && dotsData.hasEvent(mCurrentCalendar.get(Calendar.DATE))) mDotsInclusionArray?.add(dateTextView)
                }
                mCurrentCalendar.add(Calendar.DATE, 1)
            }
            mCurrentCalendar.set(Calendar.DATE, 1)
            mCurrentCalendar.set(Calendar.MONTH, mMonth)
            mCurrentCalendar.set(Calendar.YEAR, mYear)
        }

        override fun onPostExecute(finished: Boolean?) {
            var count = mDotsInclusionArray?.size ?: 0
            for (i in 0 until count) {
                try {
                    mDotsInclusionArray!![i].setHasEvent(true)
                } catch (e: Exception) {
                }
            }
            count = mDotsRemovalArray!!.size
            for (i in 0 until count) {
                mDotsRemovalArray!![i].setHasEvent(false)
            }
            mResetTaskExecuting = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthConstraints: Int = paddingLeft + paddingRight
//        val heightConstraints: Int = paddingTop + paddingBottom
        val dateWidthSpec: Int = View.MeasureSpec.makeMeasureSpec(mDateTextWidth.toInt(), View.MeasureSpec.EXACTLY)
        val dateHeightSpec: Int = View.MeasureSpec.makeMeasureSpec(mDateTextHeight.toInt(), View.MeasureSpec.EXACTLY)
        mWidth = View.getDefaultSize(0, widthMeasureSpec)
        mDateTextWidth = (mWidth - widthConstraints) / 7f
        mDateTextHeight = if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) (View.MeasureSpec.getSize(heightMeasureSpec).toFloat() - resources.getDimension(R.dimen.dimen_date_text_view) / 1.5f - resources.getDimension(R.dimen.height_week_day_header)) / mNoOfWeeks else mContext.resources.getDimension(R.dimen.dimen_date_text_view)
        mHeight = (mNoOfWeeks * mDateTextHeight).toInt()
        val childCount = childCount
        for (i in 0 until childCount) {
            getChildAt(i).measure(dateWidthSpec, dateHeightSpec)
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw != w) {
            mWidth = w
            mHeight = h
            mDateTextWidth = w / 7f
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var weekTopOffset = 0f
        for (i in 0 until mNoOfWeeks) {
            var dateLeft = 0f
            for (j in 0..6) {
                val child = getChildAt(i * 7 + j)
                child.layout(dateLeft.toInt(), weekTopOffset.toInt(), (dateLeft + mDateTextWidth).toInt(), (weekTopOffset + mDateTextHeight).toInt())
                if (isLoadingFirstTime && i * 7 + j == mSelectedDatePosition) {
                    mTranslationDistance = weekTopOffset.toInt().toFloat()
                    isLoadingFirstTime = false
                }
                dateLeft += mDateTextWidth
            }
            weekTopOffset += mDateTextHeight
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mRefreshDotsTask != null && mResetTaskExecuting) mRefreshDotsTask?.cancel(true)
    }

    override fun onDateTextViewSelected(dateTextView: DateTextView, isClick: Boolean) {
        if (sSelectedDateTextView != null && sSelectedDateTextView != dateTextView) sSelectedDateTextView?.unSelect(isClick)
        sSelectedDateTextView = dateTextView
        ZMailCalendarUtil.setCurrentSelectedDate(sSelectedDateTextView!!.date)
        if (mCallback != null) mCallback?.onDaySelected(ZMailCalendarUtil.getCurrentSelectedDate(), isClick)
        post {
            val layoutLocation = IntArray(2)
            val dateViewLocation = IntArray(2)
            this@MonthDatesGridLayout.getLocationOnScreen(layoutLocation)
            try {
                sSelectedDateTextView?.getLocationOnScreen(dateViewLocation)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            mTranslationDistance = (dateViewLocation[1] - layoutLocation[1]).toFloat()
            if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.WEEK_MODE) translationY = -mTranslationDistance
        }
    }

    fun resetWeekStartDay(weekStartDay: Int) {
        if (sWeekStartDay == weekStartDay) return
        sWeekStartDay = weekStartDay
        setCalendarProperties()
        removeAllViews()
        addChildViews()
    }

    fun selectDefaultDate(defaultDate: Int) {
        var defaultDate = defaultDate
        if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.MONTH_MODE) {
            if (defaultDate < 29) (getChildAt(mMonthStartDayOffset - 1 + defaultDate) as DateTextView).select(false)
            else {
                val dateTextView = getChildAt(mMonthStartDayOffset - 1 + defaultDate) as DateTextView
                if (!dateTextView.isCurrentMonth) selectDefaultDate(--defaultDate)
                else dateTextView.select(false)
            }
        } else {
            var finished = false
            var position = (mSelectedWeekNo - 1) * 7
            while (!finished) {
                val dateTextView = getChildAt(position) as DateTextView
                if (dateTextView.isCurrentMonth) {
                    dateTextView.select(false)
                    finished = true
                } else position++
            }
        }
    }

    fun selectDefaultDateOnPageChanged(defaultDate: Int, isUserClick: Boolean?) {
        var defaultDate = defaultDate
        var isUserClick = isUserClick
        isUserClick = false
        if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.MONTH_MODE) {
            if (defaultDate < 29) {
                (getChildAt(mMonthStartDayOffset - 1 + defaultDate) as DateTextView).selectOnPageChange(isUserClick)
            } else {
                val dateTextView = getChildAt(mMonthStartDayOffset - 1 + defaultDate) as DateTextView
                if (!dateTextView.isCurrentMonth) selectDefaultDate(--defaultDate)
                else dateTextView.selectOnPageChange(isUserClick)
            }
        } else {
            var finished = false
            var position = (mSelectedWeekNo - 1) * 7
            while (!finished) {
                val dateTextView = getChildAt(position) as DateTextView
                if (dateTextView.isCurrentMonth) {
                    dateTextView.selectOnPageChange(isUserClick)
                    finished = true
                } else position++
            }
        }
    }


    fun setCallback(callback: CallBack) {
        mCallback = callback
    }

    fun setTranslationFraction(fraction: Float) {
        this.translationY = mTranslationDistance * fraction
    }

    fun selectDate(date: Calendar) {
        val selectedDate = getChildAt(mMonthStartDayOffset - 1 + date.get(Calendar.DATE)) as DateTextView
        selectedDate.select(true)
    }

    fun setShowOnlyCurrentMonthWeeks(showOnlyCurrentMonthWeeks: Boolean) {
        MonthDatesGridLayout.showOnlyCurrentMonthWeeks = showOnlyCurrentMonthWeeks
    }

    companion object {
        private var sWeekStartDay = Calendar.SUNDAY
        private var showOnlyCurrentMonthWeeks: Boolean = false
        @SuppressLint("StaticFieldLeak")
        var sSelectedDateTextView: DateTextView? = null
        fun getSelectedDateTextView(): DateTextView? = sSelectedDateTextView
        fun clearSelectedDateTextView() {
            sSelectedDateTextView = null
        }
    }
}
