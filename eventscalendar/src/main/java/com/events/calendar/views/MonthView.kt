package com.events.calendar.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.events.calendar.R
import com.events.calendar.utils.EventsCalendarUtil
import java.util.*

class MonthView : ViewGroup, MonthDatesGridLayout.CallBack {
    private var mContext: Context? = null
    private var sWeekStartDay = Calendar.MONDAY
    private var mMonth: Int = 0
    private var mYear: Int = 0
    private var mLayoutInflater: LayoutInflater? = null
    private var mWeekDaysHeader: View? = null
    private var mFirstDay: TextView? = null
    private var mSecondDay: TextView? = null
    private var mThirdDay: TextView? = null
    private var mFourthDay: TextView? = null
    private var mFifthDay: TextView? = null
    private var mSixthDay: TextView? = null
    private var mSeventhDay: TextView? = null
    var monthGridLayout: MonthDatesGridLayout? = null
        private set
    private var mMonthGridContainer: MonthGridContainer? = null
    private var mCallback: Callback? = null
    private var mSelectedWeekNo: Int = 0
    private var mMonthTitleTextView: TextView? = null

    fun reset(doChangeWeekStartDay: Boolean) {
        if (doChangeWeekStartDay) {
            sWeekStartDay = EventsCalendarUtil.weekStartDay
            setWeekdayHeader()
            monthGridLayout?.resetWeekStartDay(sWeekStartDay)
        } else monthGridLayout?.refreshDots()
    }

    fun refreshDates() {
        monthGridLayout?.refreshToday()
    }

    interface Callback {
        fun onDaySelected(isClick: Boolean)
    }

    constructor(context: Context) : super(context) {
        init(context, -1, -1, sWeekStartDay, 1)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, -1, -1, sWeekStartDay, 1)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, -1, -1, sWeekStartDay, 1)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, -1, -1, sWeekStartDay, 1)
    }

    constructor(context: Context, month: Int, year: Int, weekStartDay: Int, selectedWeekNo: Int) : super(context) {
        init(context, month, year, weekStartDay, selectedWeekNo)
    }

    constructor(context: Context, month: Calendar, weekStartDay: Int, selectedWeekNo: Int) : super(context) {
        init(context, month.get(Calendar.MONTH), month.get(Calendar.YEAR), weekStartDay, selectedWeekNo)
    }

    private fun init(context: Context, month: Int, year: Int, weekStartDay: Int, selectedWeekNo: Int) {
        mContext = context
        mLayoutInflater = LayoutInflater.from(mContext)
        mMonth = month
        mYear = year
        sWeekStartDay = weekStartDay
        mSelectedWeekNo = selectedWeekNo
        initMonthTitle()
        initWeekDayHeader()
        setMonthGridLayout()
    }

    private fun initMonthTitle() {
        mMonthTitleTextView = mLayoutInflater?.inflate(R.layout.zmail_layout_month_title, null) as TextView
        val calendar = Calendar.getInstance()
        calendar.set(mYear, mMonth, 1)
        mMonthTitleTextView?.text = EventsCalendarUtil.getMonthString(calendar, EventsCalendarUtil.DISPLAY_STRING)
        mMonthTitleTextView?.setTextColor(EventsCalendarUtil.primaryTextColor)
        if (EventsCalendarUtil.monthTitleTypeface != null) mMonthTitleTextView?.typeface = EventsCalendarUtil.monthTitleTypeface
        mMonthTitleTextView?.setTextColor(EventsCalendarUtil.monthTitleColor)
        addView(mMonthTitleTextView)
    }

    private fun initWeekDayHeader() {
        mWeekDaysHeader = mLayoutInflater?.inflate(R.layout.zmail_layout_weekday_header, null)
        mFirstDay = mWeekDaysHeader?.findViewById(R.id.first_day)
        mSecondDay = mWeekDaysHeader?.findViewById(R.id.second_day)
        mThirdDay = mWeekDaysHeader?.findViewById(R.id.third_day)
        mFourthDay = mWeekDaysHeader?.findViewById(R.id.fourth_day)
        mFifthDay = mWeekDaysHeader?.findViewById(R.id.fifth_day)
        mSixthDay = mWeekDaysHeader?.findViewById(R.id.sixth_day)
        mSeventhDay = mWeekDaysHeader?.findViewById(R.id.seventh_day)
        setWeekdayHeader()
        addView(mWeekDaysHeader)
    }

    private fun setWeekdayHeader() {
        val headers = arrayOf(mFirstDay!!, mSecondDay!!, mThirdDay!!, mFourthDay!!, mFifthDay!!, mSixthDay!!, mSeventhDay!!)
        for (i in 0..6) {
            setWeekDayHeaderString(headers[i], if (i + sWeekStartDay > 7) (i + sWeekStartDay) % 7 else i + sWeekStartDay)
        }
    }

    private fun setWeekDayHeaderString(header: TextView, calendarConstant: Int) {
        header.setTextColor(EventsCalendarUtil.weekHeaderColor)
        if (EventsCalendarUtil.weekHeaderTypeface != null) header.typeface = EventsCalendarUtil.weekHeaderTypeface
        when (calendarConstant) {
            Calendar.SUNDAY -> header.text = mContext!!.getString(R.string.sunday)
            Calendar.MONDAY -> header.text = mContext!!.getString(R.string.monday)
            Calendar.TUESDAY -> header.text = mContext!!.getString(R.string.tuesday)
            Calendar.WEDNESDAY -> header.text = mContext!!.getString(R.string.wednesday)
            Calendar.THURSDAY -> header.text = mContext!!.getString(R.string.thursday)
            Calendar.FRIDAY -> header.text = mContext!!.getString(R.string.friday)
            Calendar.SATURDAY -> header.text = mContext!!.getString(R.string.saturday)
        }
    }

    private fun setMonthGridLayout() {
        monthGridLayout = MonthDatesGridLayout(mContext!!, mMonth, mYear, sWeekStartDay, mSelectedWeekNo)
        monthGridLayout?.setCallback(this)
        mMonthGridContainer = MonthGridContainer(mContext!!, monthGridLayout!!)
        addView(mMonthGridContainer)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mMonthTitleTextView?.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(resources.getDimension(R.dimen.height_month_title).toInt(), View.MeasureSpec.EXACTLY))
        mWeekDaysHeader?.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(mContext!!.resources.getDimension(R.dimen.height_week_day_header).toInt(), View.MeasureSpec.EXACTLY))
        mMonthGridContainer?.measure(widthMeasureSpec, heightMeasureSpec)
        val height = mMonthTitleTextView!!.measuredHeight + mWeekDaysHeader!!.measuredHeight + mMonthGridContainer!!.measuredHeight
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mMonthTitleTextView?.layout(0, 0, mMonthTitleTextView!!.measuredWidth, mMonthTitleTextView!!.measuredHeight)
        mWeekDaysHeader?.layout(0, mMonthTitleTextView!!.measuredHeight, mWeekDaysHeader!!.measuredWidth, mMonthTitleTextView!!.measuredHeight + mWeekDaysHeader!!.measuredHeight)
        mMonthGridContainer?.layout(0, mMonthTitleTextView!!.measuredHeight + mWeekDaysHeader!!.measuredHeight, mMonthGridContainer!!.measuredWidth, mMonthTitleTextView!!.measuredHeight + mWeekDaysHeader!!.measuredHeight + mMonthGridContainer!!.measuredHeight)
    }

    fun setMonthTranslationFraction(fraction: Float) {
        monthGridLayout?.setTranslationFraction(fraction)
    }

    override fun onDaySelected(date: Calendar?, isClick: Boolean) {
        if (mCallback != null) mCallback?.onDaySelected(isClick)
    }

    fun setCallback(callBack: Callback) {
        mCallback = callBack
    }

    fun onFocus(pos: Int) {
        if (pos == EventsCalendarUtil.monthPos) monthGridLayout?.selectDefaultDate(EventsCalendarUtil.selectedDate.get(Calendar.DAY_OF_MONTH))
        else monthGridLayout?.selectDefaultDateOnPageChanged(EventsCalendarUtil.tobeSelectedDate, false)
    }

    fun setSelectedDate(date: Calendar) {
        monthGridLayout?.selectDate(date)
    }
}
