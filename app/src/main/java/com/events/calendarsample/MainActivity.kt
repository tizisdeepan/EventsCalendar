package com.events.calendarsample

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.events.calendar.views.EventsCalendarCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), EventsCalendarCallback {

    override fun onDaySelected(date: Calendar?, isClick: Boolean) {
        if (date?.timeInMillis!! < eventsCalendar.minDate.timeInMillis) {

        } else if (isClick) {

        } else if (!isClick) {

        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        eventsCalendar.setToday(today)
        eventsCalendar.setMonthRange(today, end)
        eventsCalendar.setWeekStartDay(Calendar.SUNDAY, false)
        eventsCalendar.setCurrentSelectedDate(today)
        eventsCalendar.setEventDotColor(Color.parseColor("#FF0000"))
        eventsCalendar.invalidateColors()
        eventsCalendar.setDatesTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_REGULAR, this))
        eventsCalendar.setMonthTitleTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        eventsCalendar.setWeekHeaderTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        eventsCalendar.setCallback(this)

        text.setOnClickListener {
            eventsCalendar.nextPage(true)
        }
    }
}
