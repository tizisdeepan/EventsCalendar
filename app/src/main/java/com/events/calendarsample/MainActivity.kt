package com.events.calendarsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.events.calendar.views.EventsCalendar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), EventsCalendar.Callback {
    override fun onMonthChanged(monthStartDate: Calendar?) {
        Log.e("MON", "CHANGED")
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e("DAY", "SELECTED")
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
        eventsCalendar.setDatesTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_REGULAR, this))
        eventsCalendar.setMonthTitleTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        eventsCalendar.setWeekHeaderTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        eventsCalendar.setCallback(this)

        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, 6)
        eventsCalendar.addEvent(c)

        text.setOnClickListener {
            
        }
    }
}
