package com.events.calendar.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.events.calendar.views.MonthView
import java.util.Calendar

class MonthsAdapter(viewPager: EventsCalendar, startMonth: Calendar, endMonth: Calendar) : PagerAdapter() {
    private val mContext: Context = viewPager.context
    private val mMonthIterator: Calendar
    private val mCount: Int
    private val mMonthViewCallback: MonthView.Callback
    private val monthDatesGridLayoutsArray: Array<MonthView?>

    init {
        mMonthViewCallback = viewPager
        mMinMonth = if (EventsCalendarUtil.isPastDay(startMonth)) startMonth else Calendar.getInstance()
        mMaxMonth = if (EventsCalendarUtil.isFutureDay(endMonth)) endMonth else Calendar.getInstance()
        mCount = EventsCalendarUtil.getMonthCount(mMinMonth, mMaxMonth)
        monthDatesGridLayoutsArray = arrayOfNulls(mCount)
        mMonthIterator = mMinMonth.clone() as Calendar
    }

    override fun getCount(): Int = mCount

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        mMonthIterator.add(Calendar.MONTH, position)
        val monthView = MonthView(mContext, mMonthIterator, EventsCalendarUtil.weekStartDay, 1)
        monthView.setCallback(mMonthViewCallback)
        mMonthIterator.add(Calendar.MONTH, -position)
        monthDatesGridLayoutsArray[position] = monthView
        container.addView(monthView)
        return monthView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun getItem(position: Int): MonthView? = monthDatesGridLayoutsArray[position]

    companion object {
        private lateinit var mMinMonth: Calendar
        private lateinit var mMaxMonth: Calendar
    }
}
