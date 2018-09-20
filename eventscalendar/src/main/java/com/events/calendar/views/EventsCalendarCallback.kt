package com.events.calendar.views

import java.util.*

interface EventsCalendarCallback {
    fun onDaySelected(date: Calendar?, isClick: Boolean)
}