package com.events.calendar.utils

import android.util.MonthDisplayHelper
import android.util.SparseBooleanArray
import java.util.*

class EventDots(month: Calendar) {

    private val mCalendar: Calendar = month.clone() as Calendar
    private val mMonthDisplayHelper: MonthDisplayHelper = MonthDisplayHelper(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH))
    private val mNoOfDays: Int = mMonthDisplayHelper.numberOfDaysInMonth
    private val mDotsArray: SparseBooleanArray = SparseBooleanArray(mNoOfDays)

    fun clear() {
        mDotsArray.clear()
    }

    fun add(date: Int) {
        mDotsArray.put(date, true)
    }

    fun hasEvent(date: Int): Boolean {
        return mDotsArray.get(date)
    }
}
