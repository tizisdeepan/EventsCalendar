package com.event.calendar.eventcalendar.views

import java.util.*

interface EventCalendarCallback {
    fun onDaySelected(date: Calendar?, isClick: Boolean)
}