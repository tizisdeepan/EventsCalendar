package com.event.calendar.eventcalendar.views

import android.annotation.SuppressLint
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
import com.event.calendar.eventcalendar.R
import com.event.calendar.eventcalendar.adapters.MonthPageAdapter
import com.event.calendar.eventcalendar.adapters.WeekPageAdapter
import com.event.calendar.eventcalendar.utils.ZEvents
import com.event.calendar.eventcalendar.utils.ZMailCalendarUtil
import java.util.*

class EventCalendar : ViewPager, MonthView.Callback {
    var minDate: Calendar = Calendar.getInstance()
    var maxDate: Calendar = Calendar.getInstance()
    var isPagingEnabled = true //Boolean used to switch off and on EventCalendar's page change

    private var mContext: Context? = null
    private var mAttrs: AttributeSet? = null
    private var mCurrentItem: MonthView? = null
    private var mCurrentItemHeight: Int = 0
    private var mCallback: EventCalendarCallback? = null
    private var mCalendarMonthPagerAdapter: MonthPageAdapter? = null
    private var doChangeAdapter: Boolean = false
    private var mCalendarWeekPagerAdapter: WeekPageAdapter? = null
    private var mSelectedMonthPosition: Int = 0
    private var mSelectedWeekPosition: Int = 0
    private var doFocus = true

    val weekStartDay: Int
        get() = ZMailCalendarUtil.weekStartDay

    private val mOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (childCount > 0) {
                if (doFocus) {
                    if (ZMailCalendarUtil.currentMode != ZMailCalendarUtil.WEEK_MODE) mCalendarMonthPagerAdapter?.getItem(position)?.onFocus(position)
                    else mCalendarWeekPagerAdapter?.getItem(position)?.onFocus(position)
                } else doFocus = true
            }
        }
    }


    val visibleContentHeight: Float
        get() {
            val resources = resources
            return resources.getDimension(R.dimen.height_month_title) + resources.getDimension(R.dimen.height_week_day_header) + resources.getDimension(R.dimen.dimen_date_text_view)
        }

    fun setCurrentSelectedDate(selectedDate: Calendar?) {
        val position: Int
        if (isPagingEnabled) {
            doFocus = false
            if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.MONTH_MODE) {
                position = ZMailCalendarUtil.getMonthPositionForDay(selectedDate, minDate)
                setCurrentItem(position, false)
                if (mCalendarMonthPagerAdapter != null) {
                    post {
                        ZMailCalendarUtil.monthPos = currentItem
                        ZMailCalendarUtil.selectedDate = selectedDate
                        mCalendarMonthPagerAdapter!!.getItem(currentItem)?.setSelectedDate(selectedDate!!)
                        doFocus = true
                    }
                }
            }
//            else {
//                position = ZMailCalendarUtil.getWeekPosition(selectedDate!!, minDate)
//                setCurrentItem(position, false)
//                if (mCalendarWeekPagerAdapter != null) {
//                    post {
//                        mCalendarWeekPagerAdapter?.getItem(currentItem)?.setSelectedDate(selectedDate)
//                        doFocus = true
//                    }
//                }
//            }
        }
    }

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
        ZMailCalendarUtil.today = c
        ZMailCalendarUtil.setCurrentSelectedDate(c)
    }

    fun setWeekStartDay(weekStartDay: Int, doReset: Boolean) {
        ZMailCalendarUtil.weekStartDay = weekStartDay
        if (doReset) {
            mSelectedMonthPosition = ZMailCalendarUtil.getMonthPositionForDay(ZMailCalendarUtil.getCurrentSelectedDate(), minDate)
            mSelectedWeekPosition = ZMailCalendarUtil.getWeekPosition(ZMailCalendarUtil.getCurrentSelectedDate()!!, minDate)
            doChangeAdapter = true
            changeAdapter()
            mCallback!!.onDaySelected(ZMailCalendarUtil.getCurrentSelectedDate(), false)
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

        val attributes = mContext!!.obtainStyledAttributes(attrs, R.styleable.EventCalendar, 0, 0)
        try {
            ZMailCalendarUtil.primaryTextColor = attributes.getColor(R.styleable.EventCalendar_primaryTextColor, Color.BLACK)
            ZMailCalendarUtil.secondaryTextColor = attributes.getColor(R.styleable.EventCalendar_secondaryTextColor, ContextCompat.getColor(mContext!!, R.color.text_black_disabled))
            ZMailCalendarUtil.selectedTextColor = attributes.getColor(R.styleable.EventCalendar_selectedTextColor, Color.WHITE)
            ZMailCalendarUtil.selectionColor = attributes.getColor(R.styleable.EventCalendar_selectionColor, ZMailCalendarUtil.primaryTextColor)
            ZMailCalendarUtil.eventDotColor = attributes.getColor(R.styleable.EventCalendar_eventDotColor, ZMailCalendarUtil.eventDotColor)
            ZMailCalendarUtil.monthTitleColor = attributes.getColor(R.styleable.EventCalendar_monthTitleColor, ZMailCalendarUtil.eventDotColor)
            ZMailCalendarUtil.weekHeaderColor = attributes.getColor(R.styleable.EventCalendar_weekHeaderColor, ZMailCalendarUtil.eventDotColor)
            ZMailCalendarUtil.isBoldTextOnSelectionEnabled = attributes.getBoolean(R.styleable.EventCalendar_isBoldTextOnSelectionEnabled, false)
        } finally {
            attributes.recycle()
        }

        ZMailCalendarUtil.currentMode = ZMailCalendarUtil.MONTH_MODE
        ZMailCalendarUtil.setCurrentSelectedDate(Calendar.getInstance())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        var startMonth: Calendar? = Calendar.getInstance()
        var endMonth: Calendar? = Calendar.getInstance()
        if (mCallback != null) {
            startMonth = minDate
            endMonth = maxDate
        } else {
            startMonth!!.add(Calendar.MONTH, -1)
            endMonth!!.add(Calendar.MONTH, ZMailCalendarUtil.DEFAULT_NO_OF_MONTHS / 2)
        }
        mCalendarMonthPagerAdapter = MonthPageAdapter(this, startMonth, endMonth)
        mCalendarWeekPagerAdapter = WeekPageAdapter(this, startMonth, endMonth)
        adapter = mCalendarMonthPagerAdapter
        mSelectedMonthPosition = ZMailCalendarUtil.getMonthPositionForDay(ZMailCalendarUtil.getCurrentSelectedDate(), startMonth)
        mSelectedWeekPosition = ZMailCalendarUtil.getWeekPosition(ZMailCalendarUtil.getCurrentSelectedDate()!!, startMonth)
        currentItem = mSelectedMonthPosition
        addOnPageChangeListener(mOnPageChangeListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChildren(widthMeasureSpec, heightMeasureSpec)

        try {
            mCurrentItem = if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.WEEK_MODE) (adapter as WeekPageAdapter).getItem(currentItem) else (adapter as MonthPageAdapter).getItem(currentItem)
            mCurrentItemHeight = mCurrentItem!!.measuredHeight
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mCurrentItemHeight)
    }

    fun setCurrentMonthTranslationFraction(fraction: Float) {
        mCurrentItem!!.setMonthTranslationFraction(fraction)
    }

    fun setCalendarMode(mode: Int) {
        if (mode != ZMailCalendarUtil.currentMode) {
            ZMailCalendarUtil.currentMode = mode
            doChangeAdapter = true
        }
    }

    fun setCallback(callback: EventCalendarCallback) {
        mCallback = callback
    }

    override fun onDaySelected(isClick: Boolean) {
        if (mCallback != null) {
            if (!MonthDatesGridLayout.getSelectedDateTextView()?.isCurrentMonth!!) {
                val itemNo: Int = if (ZMailCalendarUtil.getCurrentSelectedDate()!!.get(Calendar.DATE) < 8) currentItem + 1 else currentItem - 1
                if (itemNo >= 0 && itemNo <= ZMailCalendarUtil.getWeekCount(minDate!!, maxDate!!)) setCurrentSelectedDate(ZMailCalendarUtil.getCurrentSelectedDate())
                else setCurrentSelectedDate(mCurrentSelectedDate)
            } else mCallback?.onDaySelected(ZMailCalendarUtil.getCurrentSelectedDate(), isClick)
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
            val currentSelectionDate = ZMailCalendarUtil.getCurrentSelectedDate()
            if (ZMailCalendarUtil.currentMode != ZMailCalendarUtil.WEEK_MODE) {
                val position = ZMailCalendarUtil.getWeekPosition(currentSelectionDate!!, minDate!!)
                setCurrentItemField(position)
                adapter = mCalendarWeekPagerAdapter
            }
//            else {
//                val position = ZMailCalendarUtil.getMonthPositionForDay(currentSelectionDate, minDate!!)
//                setCurrentItemField(position)
//                mCalendarMonthPagerAdapter
//            }
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
            val field = ViewPager::class.java.getDeclaredField("mRestoredCurItem")
            field.isAccessible = true
            field.set(this, position)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    fun setMonthRange(minMonth: Calendar, maxMonth: Calendar) {
        this.minDate = minMonth
        this.maxDate = maxMonth
        ZEvents.initialize(this.minDate, this.maxDate)
    }

    fun setPrimaryTextColor(color: Int) {
        ZMailCalendarUtil.primaryTextColor = color
    }

    fun setSecondaryTextColor(color: Int) {
        ZMailCalendarUtil.secondaryTextColor = color
    }

    fun setEventDotColor(color: Int) {
        ZMailCalendarUtil.eventDotColor = color
    }

    fun setSelectedTextColor(color: Int) {
        ZMailCalendarUtil.selectedTextColor = color
    }

    fun setMonthTitleColor(color: Int) {
        ZMailCalendarUtil.monthTitleColor = color
    }

    fun setWeekHeaderColor(color: Int) {
        ZMailCalendarUtil.weekHeaderColor = color
    }

    fun setDatesTypeface(typeface: Typeface) {
        ZMailCalendarUtil.datesTypeface = typeface
    }

    fun setMonthTitleTypeface(typeface: Typeface) {
        ZMailCalendarUtil.monthTitleTypeface = typeface
    }

    fun setWeekHeaderTypeface(typeface: Typeface) {
        ZMailCalendarUtil.weekHeaderTypeface = typeface
    }

    fun setIsBoldTextOnSelectionEnabled(enabled: Boolean) {
        ZMailCalendarUtil.isBoldTextOnSelectionEnabled = enabled
    }

    fun nextPage(smoothScroll: Boolean) {
        this@EventCalendar.setCurrentItem(this@EventCalendar.currentItem + 1, smoothScroll)
    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    fun invalidateColors() {
        DateTextView.invalidateColors()
    }

    companion object {
        private val mCurrentSelectedDate = Calendar.getInstance()
    }
}
