//$Id$
package com.events.calendar.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.events.calendar.views.MonthView

import java.util.Calendar

class MonthsAdapter(viewPager: EventsCalendar, startMonth: Calendar, endMonth: Calendar) : PagerAdapter() {
    private val mContext: Context
    private val mMonthIterator: Calendar
    private val mCount: Int
    private val mMonthViewCallback: MonthView.Callback

    private val monthDatesGridLayoutsArray: Array<MonthView?>


    init {
        mContext = viewPager.context
        mMonthViewCallback = viewPager
        if (EventsCalendarUtil.isPastDay(startMonth)) {
            mMinMonth = startMonth
        } else {
            mMinMonth = Calendar.getInstance()
        }

        if (EventsCalendarUtil.isFutureDay(endMonth)) {
            mMaxMonth = endMonth
        } else {
            mMaxMonth = Calendar.getInstance()
        }

        mCount = EventsCalendarUtil.getMonthCount(mMinMonth!!, mMaxMonth!!)
        monthDatesGridLayoutsArray = arrayOfNulls(mCount)
        mMonthIterator = mMinMonth!!.clone() as Calendar
    }

    override fun getCount(): Int {
        return mCount
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

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

    fun getItem(position: Int): MonthView? {
        return monthDatesGridLayoutsArray[position]
    }

    companion object {

        private var mMinMonth: Calendar? = null
        private var mMaxMonth: Calendar? = null
    }
}
