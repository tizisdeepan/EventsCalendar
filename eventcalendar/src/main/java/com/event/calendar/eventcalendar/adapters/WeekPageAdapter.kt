package com.event.calendar.eventcalendar.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.event.calendar.eventcalendar.utils.ZMailCalendarUtil
import com.event.calendar.eventcalendar.views.EventCalendar
import com.event.calendar.eventcalendar.views.MonthView
import java.util.*

class WeekPageAdapter(viewPager: EventCalendar, startDay: Calendar, endDay: Calendar) : PagerAdapter() {
    private val mMinMonth: Calendar
    private val mMaxMonth: Calendar
    private val mCount: Int
    private val monthDatesGridLayoutsArray: Array<MonthView?>
    private val mContext: Context = viewPager.context
    private val mMonthViewCallback: MonthView.Callback

    init {
        mMonthViewCallback = viewPager
        mMinMonth = if (ZMailCalendarUtil.isPastDay(startDay)) startDay else Calendar.getInstance()
        mMaxMonth = if (ZMailCalendarUtil.isFutureDay(endDay)) endDay else Calendar.getInstance()
        mCount = ZMailCalendarUtil.getWeekCount(mMinMonth, mMaxMonth)
        monthDatesGridLayoutsArray = arrayOfNulls(mCount)
    }

    override fun getCount(): Int = mCount

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val month = ZMailCalendarUtil.getMonthForWeekPosition(mMinMonth, position)
        val weekNo = ZMailCalendarUtil.getWeekNo(mMinMonth, position)
        val monthView = MonthView(mContext, month, ZMailCalendarUtil.weekStartDay, weekNo)
        monthView.setCallback(mMonthViewCallback)
        monthDatesGridLayoutsArray[position] = monthView
        container.addView(monthView)
        return monthView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun getItem(position: Int): MonthView? = monthDatesGridLayoutsArray[position]

    companion object {
        private val TAG = "WEEK_PAGE_ADAPTER"
    }
}
