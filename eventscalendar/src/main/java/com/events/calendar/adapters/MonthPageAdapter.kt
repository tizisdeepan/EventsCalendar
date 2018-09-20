package com.events.calendar.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.events.calendar.utils.ZMailCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.events.calendar.views.MonthView
import java.util.*

class MonthPageAdapter(viewPager: EventsCalendar, startMonth: Calendar, endMonth: Calendar) : PagerAdapter() {
    private val mContext: Context = viewPager.context
    private val mMonthIterator: Calendar?
    private val mCount: Int
    private val mMonthViewCallback: MonthView.Callback
    private val monthDatesGridLayoutsArray: Array<MonthView?>

    init {
        mMonthViewCallback = viewPager
        mMinMonth = if (ZMailCalendarUtil.isPastDay(startMonth)) startMonth else Calendar.getInstance()
        mMaxMonth = if (ZMailCalendarUtil.isFutureDay(endMonth)) endMonth else Calendar.getInstance()
        mCount = ZMailCalendarUtil.getMonthCount(mMinMonth, mMaxMonth)
        monthDatesGridLayoutsArray = arrayOfNulls(mCount)
        mMonthIterator = mMinMonth?.clone() as? Calendar
    }

    override fun getCount(): Int = mCount

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        mMonthIterator?.add(Calendar.MONTH, position)
        val monthView = MonthView(mContext, mMonthIterator!!, ZMailCalendarUtil.weekStartDay, 1)
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
        private val MONTHS_IN_YEAR = 12
        private val TAG = "MONTHLY_PAGE_ADAPTER"

        private var mMinMonth: Calendar? = null
        private var mMaxMonth: Calendar? = null
    }
}
