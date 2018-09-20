package com.events.calendar.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.events.calendar.R
import com.events.calendar.adapters.MonthPagerAdapter
import com.events.calendar.adapters.WeekPageAdapter
import com.events.calendar.utils.Events
import com.events.calendar.utils.EventsCalendarUtil
import java.util.*

class EventsCalendar : ViewPager, MonthView.Callback {
    lateinit var mMinMonth: Calendar
    lateinit var mMaxMonth: Calendar
    var isPagingEnabled = true //Boolean used to switch off and on EventsCalendar's page change

    private var mContext: Context? = null
    private var mAttrs: AttributeSet? = null
    private var mCurrentItem: MonthView? = null
    private var mCurrentItemHeight: Int = 0
    private var mCallback: Callback? = null
    private var mCalendarMonthPagerAdapter: MonthPagerAdapter? = null
    private var doChangeAdapter: Boolean = false
    private var mCalendarWeekPagerAdapter: WeekPageAdapter? = null
    private var mSelectedMonthPosition: Int = 0
    private var mSelectedWeekPosition: Int = 0
    private var doFocus = true

    val weekStartDay: Int
        get() = EventsCalendarUtil.weekStartDay

    private val mOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (childCount > 0) {
                if (doFocus) {
                    if (EventsCalendarUtil.currentMode != EventsCalendarUtil.WEEK_MODE) {
                        mCalendarMonthPagerAdapter!!.getItem(position)!!.onFocus(position)
                    }
                } else {
                    doFocus = true
                }
            }
        }
    }


    val visibleContentHeight: Float
        get() {
            val resources = resources
            return resources.getDimension(R.dimen.height_month_title) + resources.getDimension(R.dimen.height_week_day_header) + resources.getDimension(R.dimen.dimen_date_text_view)
        }

    /**
     * Interface to be implemented by Activity or Fragment which may use EventsCalendar
     */
    interface Callback {
        fun onDaySelected(selectedDate: Calendar?)
        fun onMonthChanged(monthStartDate: Calendar?)
    }

    fun setCurrentSelectedDate(selectedDate: Calendar?) {
        val position: Int
        if (isPagingEnabled) {
            doFocus = false
            if (EventsCalendarUtil.currentMode == EventsCalendarUtil.MONTH_MODE) {
                position = EventsCalendarUtil.getMonthPositionForDay(selectedDate, mMinMonth)
                setCurrentItem(position, false)
                if (mCalendarMonthPagerAdapter != null) {
                    post {
                        EventsCalendarUtil.monthPos = currentItem
                        EventsCalendarUtil.selectedDate = selectedDate
                        mCalendarMonthPagerAdapter!!.getItem(currentItem)!!.setSelectedDate(selectedDate!!)
                        doFocus = true
                    }
                }
            }
            //            else
            //            {
            //                position = EventsCalendarUtil.getWeekPosition(selectedDate, mMinMonth);
            //                setCurrentItem(position, false);
            //                if(mCalendarWeekPagerAdapter!=null)
            //                {
            //                    post(new Runnable()
            //                    {
            //                        @Override
            //                        public void run()
            //                        {
            //                            mCalendarWeekPagerAdapter.getItem(getCurrentItem()).setSelectedDate(selectedDate);
            //                            doFocus = true;
            //                        }
            //                    });
            //                }
            //            }
        }
    }

    fun getCurrentSelectedDate(): Calendar? = EventsCalendarUtil.selectedDate

    fun reset() {
        for (i in 0 until childCount) {
            (getChildAt(i) as MonthView).reset(false)
        }
    }

    fun refreshTodayDate() {
        for (i in 0 until childCount) {
            (getChildAt(i) as MonthView).refreshDates()
        }
    }

    fun setToday(c: Calendar) {
        EventsCalendarUtil.today = c
        EventsCalendarUtil.setCurrentSelectedDate(c)
    }

    fun setWeekStartDay(weekStartDay: Int, doReset: Boolean) {
        EventsCalendarUtil.weekStartDay = weekStartDay
        if (doReset) {
            mSelectedMonthPosition = EventsCalendarUtil.getMonthPositionForDay(EventsCalendarUtil.getCurrentSelectedDate(), mMinMonth)
            mSelectedWeekPosition = EventsCalendarUtil.getWeekPosition(EventsCalendarUtil.getCurrentSelectedDate()!!, mMinMonth)
            doChangeAdapter = true
            changeAdapter()
            mCallback?.onDaySelected(EventsCalendarUtil.getCurrentSelectedDate())
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        mAttrs = attrs

        val attributes = mContext!!.obtainStyledAttributes(attrs, R.styleable.EventsCalendar, 0, 0)
        try {
            EventsCalendarUtil.primaryTextColor = attributes.getColor(R.styleable.EventsCalendar_primaryTextColor, Color.BLACK)
            EventsCalendarUtil.secondaryTextColor = attributes.getColor(R.styleable.EventsCalendar_secondaryTextColor, ContextCompat.getColor(mContext!!, R.color.text_black_disabled))
            EventsCalendarUtil.selectedTextColor = attributes.getColor(R.styleable.EventsCalendar_selectedTextColor, Color.WHITE)
            EventsCalendarUtil.selectionColor = attributes.getColor(R.styleable.EventsCalendar_selectionColor, EventsCalendarUtil.primaryTextColor)
            EventsCalendarUtil.eventDotColor = attributes.getColor(R.styleable.EventsCalendar_eventDotColor, EventsCalendarUtil.eventDotColor)
            EventsCalendarUtil.monthTitleColor = attributes.getColor(R.styleable.EventsCalendar_monthTitleColor, EventsCalendarUtil.secondaryTextColor)
            EventsCalendarUtil.weekHeaderColor = attributes.getColor(R.styleable.EventsCalendar_weekHeaderColor, EventsCalendarUtil.secondaryTextColor)
            EventsCalendarUtil.isBoldTextOnSelectionEnabled = attributes.getBoolean(R.styleable.EventsCalendar_isBoldTextOnSelectionEnabled, false)
        } finally {
            attributes.recycle()
        }

        EventsCalendarUtil.currentMode = EventsCalendarUtil.MONTH_MODE
        EventsCalendarUtil.setCurrentSelectedDate(Calendar.getInstance())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        var startMonth = Calendar.getInstance()
        var endMonth = Calendar.getInstance()
        if (mCallback != null) {
            startMonth = mMinMonth
            endMonth = mMaxMonth
        } else {
            startMonth.add(Calendar.MONTH, -1)
            endMonth.add(Calendar.MONTH, EventsCalendarUtil.DEFAULT_NO_OF_MONTHS / 2)
        }
        mCalendarMonthPagerAdapter = MonthPagerAdapter(this, startMonth, endMonth)
        mCalendarWeekPagerAdapter = WeekPageAdapter(this, startMonth, endMonth)
        adapter = mCalendarMonthPagerAdapter
        mSelectedMonthPosition = EventsCalendarUtil.getMonthPositionForDay(EventsCalendarUtil.getCurrentSelectedDate(), startMonth)
        mSelectedWeekPosition = EventsCalendarUtil.getWeekPosition(EventsCalendarUtil.getCurrentSelectedDate()!!, startMonth)
        currentItem = mSelectedMonthPosition
        addOnPageChangeListener(mOnPageChangeListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChildren(widthMeasureSpec, heightMeasureSpec)

        try {
            mCurrentItem = if (EventsCalendarUtil.currentMode == EventsCalendarUtil.WEEK_MODE) (adapter as WeekPageAdapter).getItem(currentItem)
            else (adapter as MonthPagerAdapter).getItem(currentItem)
            mCurrentItemHeight = mCurrentItem!!.measuredHeight
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mCurrentItemHeight)
    }

    fun setCurrentMonthTranslationFraction(fraction: Float) {
        mCurrentItem!!.setMonthTranslationFraction(fraction)
    }

    fun setCalendarMode(mode: Int) {
        if (mode != EventsCalendarUtil.currentMode) {
            EventsCalendarUtil.currentMode = mode
            doChangeAdapter = true
        }
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    override fun onDaySelected(isClick: Boolean) {
        if (mCallback != null) {
            if (!MonthDatesGridLayout.selectedDateTextView?.isCurrentMonth!!) {
                val itemNo: Int
                if (EventsCalendarUtil.getCurrentSelectedDate()!!.get(Calendar.DATE) < 8) {
                    itemNo = currentItem + 1
                } else {
                    itemNo = currentItem - 1
                }
                if (itemNo >= 0 && itemNo <= EventsCalendarUtil.getWeekCount(mMinMonth, mMaxMonth)) {
                    setCurrentSelectedDate(EventsCalendarUtil.getCurrentSelectedDate())
                }
            } else {
                if (isClick) {
                    setCurrentSelectedDate(EventsCalendarUtil.getCurrentSelectedDate())
                    mCallback?.onDaySelected(EventsCalendarUtil.getCurrentSelectedDate())
                } else {
                    mCallback?.onMonthChanged(EventsCalendarUtil.getCurrentSelectedDate())
                }
            }
        }
    }

    fun changeAdapter() {
        if (doChangeAdapter) {
            MonthDatesGridLayout.clearSelectedDateTextView()
            val parcel = Parcel.obtain()
            if (Build.VERSION.SDK_INT >= 23) {
                parcel.writeParcelable(null, 0)
            }
            parcel.writeParcelable(null, 0)
            val currentSelectionDate = EventsCalendarUtil.getCurrentSelectedDate()
            if (EventsCalendarUtil.currentMode == EventsCalendarUtil.WEEK_MODE) {
                val position = EventsCalendarUtil.getWeekPosition(currentSelectionDate!!, mMinMonth)
                setCurrentItemField(position)
                adapter = mCalendarWeekPagerAdapter
            } else {
                val position = EventsCalendarUtil.getMonthPositionForDay(currentSelectionDate, mMinMonth)
                setCurrentItemField(position)
                adapter = mCalendarMonthPagerAdapter
            }
            doChangeAdapter = false
        }
    }

    //TODO: If we use Proguard, then you need to include the following in its config:
    //-keepclassmembers class android.support.v4.view.ViewPager
    //{
    //      private int mRestoredCurItem;
    //}
    private fun setCurrentItemField(position: Int) {
        try {
            val field = ViewPager::class.java.getDeclaredField("mRestoredCurItem")//No I18N
            field.isAccessible = true
            field.set(this, position)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    fun setMonthRange(minMonth: Calendar, maxMonth: Calendar) {
        mMinMonth = minMonth
        mMaxMonth = maxMonth
        Events.initialize(mMinMonth, mMaxMonth)
    }

    fun setPrimaryTextColor(color: Int) {
        EventsCalendarUtil.primaryTextColor = color
    }

    fun setSecondaryTextColor(color: Int) {
        EventsCalendarUtil.secondaryTextColor = color
    }

    fun setEventDotColor(color: Int) {
        EventsCalendarUtil.eventDotColor = color
    }

    fun setSelectedTextColor(color: Int) {
        EventsCalendarUtil.selectedTextColor = color
    }

    fun setMonthTitleColor(color: Int){
        EventsCalendarUtil.monthTitleColor = color
    }

    fun setWeekHeaderColor(color: Int){
        EventsCalendarUtil.weekHeaderColor = color
    }

    fun setDatesTypeface(typeface: Typeface){
        EventsCalendarUtil.datesTypeface = typeface
    }

    fun setMonthTitleTypeface(typeface: Typeface){
        EventsCalendarUtil.monthTitleTypeface = typeface
    }

    fun setWeekHeaderTypeface(typeface: Typeface){
        EventsCalendarUtil.weekHeaderTypeface = typeface
    }

    fun setIsBoldTextOnSelectionEnabled(isEnabled: Boolean){
        EventsCalendarUtil.isBoldTextOnSelectionEnabled = isEnabled
    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    fun invalidateColors() {
        DateTextView.invalidateColors()
    }

    companion object {
        private val TAG = "EventsCalendar"
        private val mCurrentSelectedDate = Calendar.getInstance()
    }
}
