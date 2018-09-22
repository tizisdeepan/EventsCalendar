package com.events.calendar.utils

import java.util.*

object Events {
    private val TAG = "Events"

    var startMonth = Calendar.getInstance()
    var endMonth = Calendar.getInstance()
    var totalNoOfMonths: Int = 0
    private var mDotsMap: HashMap<String, EventDots>? = null


    fun initialize(startMonth: Calendar, endMonth: Calendar) {
        Events.startMonth = startMonth
        Events.endMonth = endMonth
        totalNoOfMonths = EventsCalendarUtil.getMonthCount(startMonth, endMonth)
        mDotsMap = HashMap(totalNoOfMonths)
        createEventsDotsStorage()
    }

    private fun createEventsDotsStorage() {
        val iterator = startMonth.clone() as Calendar
        for (i in 0 until totalNoOfMonths) {
            val monthString = EventsCalendarUtil.getMonthString(iterator, EventsCalendarUtil.YYYY_MM)
            val eventDots = EventDots(iterator)
            mDotsMap!![monthString!!] = eventDots
            iterator.add(Calendar.MONTH, 1)
        }
    }

    fun clear() {
        val iterator = startMonth.clone() as Calendar
        for (i in 0 until mDotsMap!!.size) {
            val monthString = EventsCalendarUtil.getMonthString(iterator, EventsCalendarUtil.YYYY_MM)
            mDotsMap!![monthString]?.clear()
            iterator.add(Calendar.MONTH, 1)
        }
    }

    fun add(date: String) {
        val monthCalendar = EventsCalendarUtil.getCalendar(date, EventsCalendarUtil.YYYY_MM_DD)
        val dots = mDotsMap!![date.substring(0, 7)]
        dots?.add(monthCalendar!!.get(Calendar.DAY_OF_MONTH))
    }

    fun add(c: Calendar) {
        val date = EventsCalendarUtil.getDateString(c, EventsCalendarUtil.YYYY_MM_DD) ?: ""
        if (!date.isEmpty()) {
            val dots = mDotsMap!![date.substring(0, 7)]
            dots?.add(c[Calendar.DAY_OF_MONTH])
        }
    }

    fun hasEvent(calendar: Calendar): Boolean {
        val eventDots = getDotsForMonth(calendar)
        return eventDots!!.hasEvent(calendar.get(Calendar.DATE))
    }

    fun getDotsForMonth(monthCalendar: Calendar): EventDots? {
        return getDotsForMonth(EventsCalendarUtil.getMonthString(monthCalendar, EventsCalendarUtil.YYYY_MM))
    }

    fun getDotsForMonth(monthString: String?): EventDots? {
        return if (mDotsMap != null) {
            mDotsMap!![monthString]
        } else {
            null
        }
    }

    fun isWithinMonthSpan(mDate: Calendar): Boolean {
        val year = mDate.get(Calendar.YEAR)
        val month = mDate.get(Calendar.MONTH)
        if (year < startMonth.get(Calendar.YEAR) && year > endMonth.get(Calendar.YEAR)) {
            return false
        } else {
            if (year == startMonth.get(Calendar.YEAR) && month < startMonth.get(Calendar.MONTH)) {
                return false
            } else if (year == endMonth.get(Calendar.YEAR) && month > endMonth.get(Calendar.MONTH)) {
                return false
            }
        }
        return true
    }
}
