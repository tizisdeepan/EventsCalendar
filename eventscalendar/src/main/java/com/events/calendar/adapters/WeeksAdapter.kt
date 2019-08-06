//$Id$
package com.events.calendar.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.events.calendar.views.MonthView
import java.util.*

class WeeksAdapter(viewPager: EventsCalendar, startDay: Calendar, endDay: Calendar) : PagerAdapter() {
    private val mMinMonth: Calendar
    private val mMaxMonth: Calendar
    private val mCount: Int
    private val monthDatesGridLayoutsArray: Array<MonthView?>
    private val mContext: Context = viewPager.context
    private val mMonthViewCallback: MonthView.Callback

    init {
        mMonthViewCallback = viewPager
        mMinMonth = if (EventsCalendarUtil.isPastDay(startDay)) startDay else Calendar.getInstance()
        mMaxMonth = if (EventsCalendarUtil.isFutureDay(endDay)) endDay else Calendar.getInstance()
        mCount = EventsCalendarUtil.getWeekCount(mMinMonth, mMaxMonth)
        monthDatesGridLayoutsArray = arrayOfNulls(mCount)
    }

    override fun getCount(): Int = mCount

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val month = EventsCalendarUtil.getMonthForWeekPosition(mMinMonth, position)
        val weekNo = EventsCalendarUtil.getWeekNo(mMinMonth, position)
        val monthView = MonthView(mContext, month, EventsCalendarUtil.weekStartDay, weekNo)
        monthView.setCallback(mMonthViewCallback)
        monthDatesGridLayoutsArray[position] = monthView
        container.addView(monthView)
        return monthView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun getItem(position: Int): MonthView? = monthDatesGridLayoutsArray[position]
}
