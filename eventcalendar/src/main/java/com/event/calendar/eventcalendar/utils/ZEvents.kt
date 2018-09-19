package com.event.calendar.eventcalendar.utils

import java.util.*

object ZEvents {

    private val TAG = "ZEvents"
    var startMonth = Calendar.getInstance()
    var endMonth = Calendar.getInstance()
    var totalNoOfMonths: Int = 0
    private var mDotsMap: HashMap<String, EventDots>? = null

    fun initialize(startMonth: Calendar, endMonth: Calendar) {
        ZEvents.startMonth = startMonth
        ZEvents.endMonth = endMonth
        totalNoOfMonths = ZMailCalendarUtil.getMonthCount(startMonth, endMonth)
        mDotsMap = HashMap(totalNoOfMonths)
        createEventsDotsStorage()
    }

    private fun createEventsDotsStorage() {
        val iterator = startMonth.clone() as Calendar
        for (i in 0 until totalNoOfMonths) {
            val monthString = ZMailCalendarUtil.getMonthString(iterator, ZMailCalendarUtil.YYYY_MM)
            val eventDots = EventDots(iterator)
            mDotsMap!![monthString!!] = eventDots
            iterator.add(Calendar.MONTH, 1)
        }
    }

    fun clear() {
        val iterator = startMonth.clone() as Calendar
        for (i in 0 until mDotsMap?.size!!) {
            val monthString = ZMailCalendarUtil.getMonthString(iterator, ZMailCalendarUtil.YYYY_MM)
            mDotsMap?.get(monthString)?.clear()
            iterator.add(Calendar.MONTH, 1)
        }
    }

    fun add(date: String) {
        val monthCalendar = ZMailCalendarUtil.getCalendar(date, ZMailCalendarUtil.YYYY_MM_DD)
        val dots = mDotsMap!![date.substring(0, 7)]
        dots?.add(monthCalendar!!.get(Calendar.DAY_OF_MONTH))
    }

    fun hasEvent(calendar: Calendar): Boolean {
        val eventDots = getDotsForMonth(calendar)
        return eventDots!!.hasEvent(calendar.get(Calendar.DATE))
    }

    fun getDotsForMonth(monthCalendar: Calendar): EventDots? {
        return getDotsForMonth(ZMailCalendarUtil.getMonthString(monthCalendar, ZMailCalendarUtil.YYYY_MM))
    }

    fun getDotsForMonth(monthString: String?): EventDots? = mDotsMap?.get(monthString)

    fun logData() {
        val iterator = startMonth.clone() as Calendar
        for (i in 0 until totalNoOfMonths) {
            val monthString = ZMailCalendarUtil.getMonthString(iterator, ZMailCalendarUtil.YYYY_MM)
            iterator.add(Calendar.MONTH, 1)
        }
    }

    fun isWithinMonthSpan(mDate: Calendar?): Boolean {
        if (mDate != null) {
            val year = mDate.get(Calendar.YEAR)
            val month = mDate.get(Calendar.MONTH)
            if (year < startMonth.get(Calendar.YEAR) && year > endMonth.get(Calendar.YEAR)) return false
            else {
                if (year == startMonth.get(Calendar.YEAR) && month < startMonth.get(Calendar.MONTH)) return false
                else if (year == endMonth.get(Calendar.YEAR) && month > endMonth.get(Calendar.MONTH)) return false
            }
            return true
        } else return false
    }
}
