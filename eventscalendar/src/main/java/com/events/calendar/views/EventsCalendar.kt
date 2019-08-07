package com.events.calendar.views

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.events.calendar.R
import com.events.calendar.adapters.MonthsAdapter
import com.events.calendar.adapters.WeeksAdapter
import com.events.calendar.utils.EventDots
import com.events.calendar.utils.Events
import com.events.calendar.utils.EventsCalendarUtil
import java.util.*
import kotlin.collections.ArrayList

class EventsCalendar : ViewPager, MonthView.Callback {

    lateinit var mMinMonth: Calendar
    lateinit var mMaxMonth: Calendar
    var isPagingEnabled = true

    val WEEK_MODE = 0
    val MONTH_MODE = 1

    val SINGLE_SELECTION = 0
    val RANGE_SELECTION = 1
    val MULTIPLE_SELECTION = 2

    private lateinit var mContext: Context
    private var mAttrs: AttributeSet? = null
    private var mCurrentItem: MonthView? = null
    private var mCurrentItemHeight = 0
    private var mCallback: Callback? = null
    private var mCalendarMonthsAdapter: MonthsAdapter? = null
    private var doChangeAdapter = false
    private lateinit var mCalendarWeekPagerAdapter: WeeksAdapter
    private var mSelectedMonthPosition = 0
    private var mSelectedWeekPosition = 0
    private var doFocus = true
    val weekStartDay get() = EventsCalendarUtil.weekStartDay
    val visibleContentHeight: Float
        get() {
            val resources = resources
            return resources.getDimension(R.dimen.height_month_title) + resources.getDimension(R.dimen.height_week_day_header) + resources.getDimension(R.dimen.dimen_date_text_view)
        }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        mAttrs = attrs

        val attributes = mContext.obtainStyledAttributes(attrs, R.styleable.EventsCalendar, 0, 0)
        try {
            EventsCalendarUtil.primaryTextColor = attributes.getColor(R.styleable.EventsCalendar_primaryTextColor, Color.BLACK)
            EventsCalendarUtil.secondaryTextColor = attributes.getColor(R.styleable.EventsCalendar_secondaryTextColor, ContextCompat.getColor(mContext, R.color.text_black_disabled))
            EventsCalendarUtil.selectedTextColor = attributes.getColor(R.styleable.EventsCalendar_selectedTextColor, Color.WHITE)
            EventsCalendarUtil.selectionColor = attributes.getColor(R.styleable.EventsCalendar_selectionColor, EventsCalendarUtil.primaryTextColor)
            EventsCalendarUtil.rangeSelectionColor = attributes.getColor(R.styleable.EventsCalendar_rangeSelectionColor, EventsCalendarUtil.primaryTextColor)
            EventsCalendarUtil.rangeSelectionStartColor = attributes.getColor(R.styleable.EventsCalendar_rangeSelectionStartColor, EventsCalendarUtil.primaryTextColor)
            EventsCalendarUtil.rangeSelectionEndColor = attributes.getColor(R.styleable.EventsCalendar_rangeSelectionEndColor, EventsCalendarUtil.primaryTextColor)
            EventsCalendarUtil.eventDotColor = attributes.getColor(R.styleable.EventsCalendar_eventDotColor, EventsCalendarUtil.eventDotColor)
            EventsCalendarUtil.monthTitleColor = attributes.getColor(R.styleable.EventsCalendar_monthTitleColor, EventsCalendarUtil.secondaryTextColor)
            EventsCalendarUtil.weekHeaderColor = attributes.getColor(R.styleable.EventsCalendar_weekHeaderColor, EventsCalendarUtil.secondaryTextColor)
            EventsCalendarUtil.isBoldTextOnSelectionEnabled = attributes.getBoolean(R.styleable.EventsCalendar_isBoldTextOnSelectionEnabled, false)
            EventsCalendarUtil.dateTextFontSize = EventsCalendarUtil.convertPixelsToDp(attributes.getDimension(R.styleable.EventsCalendar_datesTextSize, mContext.resources.getDimension(R.dimen.text_calendar_date)), mContext)
            EventsCalendarUtil.weekHeaderFontSize = EventsCalendarUtil.convertPixelsToDp(attributes.getDimension(R.styleable.EventsCalendar_weekHeaderTextSize, mContext.resources.getDimension(R.dimen.text_week_day_header)), mContext)
            EventsCalendarUtil.monthTitleFontSize = EventsCalendarUtil.convertPixelsToDp(attributes.getDimension(R.styleable.EventsCalendar_monthTitleTextSize, mContext.resources.getDimension(R.dimen.text_month_title)), mContext)
        } finally {
            attributes.recycle()
        }

        EventsCalendarUtil.currentMode = EventsCalendarUtil.MONTH_MODE
        EventsCalendarUtil.setCurrentSelectedDate(Calendar.getInstance())
    }

    fun build() {
        var startMonth = Calendar.getInstance()
        var endMonth = Calendar.getInstance()
        if (mCallback != null) {
            startMonth = mMinMonth
            endMonth = mMaxMonth
        } else {
            startMonth.add(Calendar.MONTH, -1)
            endMonth.add(Calendar.MONTH, EventsCalendarUtil.DEFAULT_NO_OF_MONTHS / 2)
        }
        mCalendarMonthsAdapter = MonthsAdapter(this, startMonth, endMonth)
        mCalendarWeekPagerAdapter = WeeksAdapter(this, startMonth, endMonth)
        adapter = mCalendarMonthsAdapter
        mSelectedMonthPosition = EventsCalendarUtil.getMonthPositionForDay(EventsCalendarUtil.getCurrentSelectedDate(), startMonth)
        mSelectedWeekPosition = EventsCalendarUtil.getWeekPosition(EventsCalendarUtil.getCurrentSelectedDate()!!, startMonth)
        currentItem = mSelectedMonthPosition
        addOnPageChangeListener(mOnPageChangeListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChildren(widthMeasureSpec, heightMeasureSpec)

        try {
            mCurrentItem = if (EventsCalendarUtil.currentMode == EventsCalendarUtil.WEEK_MODE) (adapter as WeeksAdapter).getItem(currentItem)
            else (adapter as MonthsAdapter).getItem(currentItem)
            mCurrentItemHeight = mCurrentItem?.measuredHeight ?: 0
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mCurrentItemHeight)
    }

    fun setCurrentMonthTranslationFraction(fraction: Float) {
        mCurrentItem?.setMonthTranslationFraction(fraction)
    }

    fun setCalendarMode(mode: Int) {
        if (mode != EventsCalendarUtil.currentMode) {
            EventsCalendarUtil.currentMode = mode
            doChangeAdapter = true
        }
    }

    fun setCallback(callback: Callback): EventsCalendar {
        mCallback = callback
        return this
    }

    override fun onDaySelected(isClick: Boolean) {
        if (mCallback != null) {
            if (!DatesGridLayout.selectedDateText?.isCurrentMonth!!) {
                val itemNo = if (EventsCalendarUtil.getCurrentSelectedDate().get(Calendar.DATE) < 8) currentItem + 1 else currentItem - 1
                if (itemNo >= 0 && itemNo <= EventsCalendarUtil.getWeekCount(mMinMonth, mMaxMonth)) {
                    setCurrentSelectedDate(EventsCalendarUtil.getCurrentSelectedDate())
                }
            } else {
                if (isClick) {
                    setCurrentSelectedDate(EventsCalendarUtil.getCurrentSelectedDate())
                    mCallback?.onDaySelected(EventsCalendarUtil.getCurrentSelectedDate())
                } else mCallback?.onMonthChanged(EventsCalendarUtil.getCurrentSelectedDate())
            }
        }
    }

    override fun onDayLongSelected(date: Calendar, isClick: Boolean) {
        mCallback?.onDayLongPressed(date)
    }

    @SuppressLint("Recycle")
    private fun changeAdapter() {
        if (doChangeAdapter) {
            DatesGridLayout.clearSelectedDateTextView()
            val parcel = Parcel.obtain()
            if (Build.VERSION.SDK_INT >= 23) parcel.writeParcelable(null, 0)
            parcel.writeParcelable(null, 0)
            val currentSelectionDate = EventsCalendarUtil.getCurrentSelectedDate()
            adapter = if (EventsCalendarUtil.currentMode == EventsCalendarUtil.WEEK_MODE) {
                val position = EventsCalendarUtil.getWeekPosition(currentSelectionDate, mMinMonth)
                setCurrentItemField(position)
                mCalendarWeekPagerAdapter
            } else {
                val position = EventsCalendarUtil.getMonthPositionForDay(currentSelectionDate, mMinMonth)
                setCurrentItemField(position)
                mCalendarMonthsAdapter
            }
            doChangeAdapter = false
        }
    }

    private fun setCurrentItemField(position: Int) {
        try {
            val field = ViewPager::class.java.getDeclaredField("mRestoredCurItem")
            field.isAccessible = true
            field.set(this, position)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    fun setMonthRange(minMonth: Calendar, maxMonth: Calendar): EventsCalendar {
        mMinMonth = minMonth
        mMaxMonth = maxMonth
        Events.initialize(mMinMonth, mMaxMonth)
        return this
    }

    fun setDateTextFontSize(size: Float): EventsCalendar {
        EventsCalendarUtil.dateTextFontSize = size
        return this
    }

    fun setWeekHeaderFontSize(size: Float): EventsCalendar {
        EventsCalendarUtil.weekHeaderFontSize = size
        return this
    }

    fun setMonthTitleFontSize(size: Float): EventsCalendar {
        EventsCalendarUtil.monthTitleFontSize = size
        return this
    }

    fun setPrimaryTextColor(color: Int): EventsCalendar {
        EventsCalendarUtil.primaryTextColor = color
        return this
    }

    fun setSecondaryTextColor(color: Int): EventsCalendar {
        EventsCalendarUtil.secondaryTextColor = color
        return this
    }

    fun setEventDotColor(color: Int): EventsCalendar {
        EventsCalendarUtil.eventDotColor = color
        return this
    }

    fun setSelectedTextColor(color: Int): EventsCalendar {
        EventsCalendarUtil.selectedTextColor = color
        return this
    }

    fun setSelectionColor(color: Int): EventsCalendar {
        EventsCalendarUtil.selectionColor = color
        return this
    }

    fun setRangeSelectionColor(color: Int): EventsCalendar {
        EventsCalendarUtil.selectionColor = color
        return this
    }

    fun setRangeSelectionStartColor(color: Int): EventsCalendar {
        EventsCalendarUtil.selectionColor = color
        return this
    }

    fun setRangeSelectionEndColor(color: Int): EventsCalendar {
        EventsCalendarUtil.selectionColor = color
        return this
    }

    fun getDatesFromSelectedRange(): ArrayList<Calendar> {
        val dates: ArrayList<Calendar> = ArrayList()
        dates.addAll(EventsCalendarUtil.datesInSelectedRange.values)
        return dates
    }

    fun setMonthTitleColor(color: Int): EventsCalendar {
        EventsCalendarUtil.monthTitleColor = color
        return this
    }

    fun setWeekHeaderColor(color: Int): EventsCalendar {
        EventsCalendarUtil.weekHeaderColor = color
        return this
    }

    fun setDatesTypeface(typeface: Typeface): EventsCalendar {
        EventsCalendarUtil.datesTypeface = typeface
        return this
    }

    fun setMonthTitleTypeface(typeface: Typeface): EventsCalendar {
        EventsCalendarUtil.monthTitleTypeface = typeface
        return this
    }

    fun setWeekHeaderTypeface(typeface: Typeface): EventsCalendar {
        EventsCalendarUtil.weekHeaderTypeface = typeface
        return this
    }

    fun setIsBoldTextOnSelectionEnabled(isEnabled: Boolean): EventsCalendar {
        EventsCalendarUtil.isBoldTextOnSelectionEnabled = isEnabled
        return this
    }

    fun setSelectionMode(mode: Int): EventsCalendar {
        EventsCalendarUtil.SELECTION_MODE = mode
        return this
    }

    fun addEvent(date: String) {
        Events.add(date)
    }

    fun addEvent(c: Calendar) {
        Events.add(c)
    }

    fun addEvent(arrayOfCalendars: Array<Calendar>) {
        for (c in arrayOfCalendars) {
            addEvent(c)
        }
    }

    fun nextPage(smoothScroll: Boolean = true) {
        setCurrentItem(currentItem + 1, smoothScroll)
    }

    fun previousPage(smoothScroll: Boolean = true) {
        setCurrentItem(currentItem - 1, smoothScroll)
    }

    fun clearEvents() {
        Events.clear()
    }

    fun disableDate(c: Calendar) {
        EventsCalendarUtil.disabledDates.add(c)
    }

    fun disableDaysInWeek(vararg days: Int): EventsCalendar {
        EventsCalendarUtil.disabledDays.clear()
        for (day in days) {
            EventsCalendarUtil.disabledDays.add(day)
        }
        return this
    }

    fun hasEvent(c: Calendar): Boolean = Events.hasEvent(c)

    fun getDotsForMonth(monthCalendar: Calendar): EventDots? = Events.getDotsForMonth(monthCalendar)

    fun getDotsForMonth(monthString: String?): EventDots? = Events.getDotsForMonth(monthString)


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = if (this.isPagingEnabled) super.onInterceptTouchEvent(event) else false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = if (this.isPagingEnabled) super.onTouchEvent(event) else false

    fun invalidateColors() {
        DateText.invalidateColors()
    }

    private val mOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (childCount > 0) {
                if (doFocus) if (EventsCalendarUtil.currentMode != EventsCalendarUtil.WEEK_MODE) mCalendarMonthsAdapter?.getItem(position)?.onFocus(position)
                else doFocus = true
            }
        }
    }

    interface Callback {
        fun onDaySelected(selectedDate: Calendar?)
        fun onDayLongPressed(selectedDate: Calendar?)
        fun onMonthChanged(monthStartDate: Calendar?)
    }

    fun setCurrentSelectedDate(selectedDate: Calendar): EventsCalendar {
        when (EventsCalendarUtil.SELECTION_MODE) {
            SINGLE_SELECTION -> {
                val position: Int
                if (isPagingEnabled) {
                    doFocus = false
                    if (EventsCalendarUtil.currentMode == EventsCalendarUtil.MONTH_MODE) {
                        position = EventsCalendarUtil.getMonthPositionForDay(selectedDate, mMinMonth)
                        setCurrentItem(position, false)
                        // post {
                        EventsCalendarUtil.monthPos = currentItem
                        EventsCalendarUtil.selectedDate = selectedDate
                        mCalendarMonthsAdapter?.getItem(currentItem)?.setSelectedDate(selectedDate!!)
                        doFocus = true
                        //}
                    } else {
                        position = EventsCalendarUtil.getWeekPosition(selectedDate, mMinMonth)
                        setCurrentItem(position, false)
                        post {
                            mCalendarWeekPagerAdapter.getItem(currentItem)?.setSelectedDate(selectedDate!!)
                            doFocus = true
                        }
                    }
                }
            }
            RANGE_SELECTION -> {
                EventsCalendarUtil.updateMinMaxDateInRange(selectedDate)
                reset()
                refreshTodayDate()
            }
            MULTIPLE_SELECTION -> {
                EventsCalendarUtil.updateSelectedDates(selectedDate)
                reset()
                refreshTodayDate()
            }
            else -> {
            }
        }
        return this
    }

    fun getCurrentSelectedDate(): Calendar? = EventsCalendarUtil.selectedDate

    private fun reset() {
        for (i in 0 until childCount) {
            (getChildAt(i) as MonthView).reset(false)
        }
    }

    private fun refreshTodayDate() {
        for (i in 0 until childCount) {
            (getChildAt(i) as MonthView).refreshDates()
        }
    }

    fun setToday(c: Calendar): EventsCalendar {
        EventsCalendarUtil.today = c
        return this
    }

    fun setWeekStartDay(weekStartDay: Int, doReset: Boolean): EventsCalendar {
        EventsCalendarUtil.weekStartDay = weekStartDay
        if (doReset) {
            mSelectedMonthPosition = EventsCalendarUtil.getMonthPositionForDay(EventsCalendarUtil.getCurrentSelectedDate(), mMinMonth)
            mSelectedWeekPosition = EventsCalendarUtil.getWeekPosition(EventsCalendarUtil.getCurrentSelectedDate(), mMinMonth)
            doChangeAdapter = true
            changeAdapter()
            mCallback?.onDaySelected(EventsCalendarUtil.getCurrentSelectedDate())
        }
        return this
    }

}
