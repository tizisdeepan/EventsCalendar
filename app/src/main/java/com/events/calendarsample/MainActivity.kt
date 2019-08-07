package com.events.calendarsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), EventsCalendar.Callback {

    override fun onDayLongPressed(selectedDate: Calendar?) {
        Log.e("LONG CLICKED", EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY))
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        Log.e("MON", "CHANGED")
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e("CLICKED", EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY))
        selected.text = getDateString(selectedDate?.timeInMillis)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selected.text = getDateString(eventsCalendar.getCurrentSelectedDate()?.timeInMillis)

        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        eventsCalendar.setSelectionMode(eventsCalendar.MULTIPLE_SELECTION)
                .setToday(today)
                .setMonthRange(today, end)
                .setWeekStartDay(Calendar.SUNDAY, false)
                .setIsBoldTextOnSelectionEnabled(true)
                .setDatesTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_REGULAR, this))
                .setMonthTitleTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
                .setWeekHeaderTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
                .setCallback(this)
                .build()

        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, 2)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 3)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 4)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 7)
        eventsCalendar.addEvent(c)
        c.add(Calendar.MONTH, 1)
        c[Calendar.DAY_OF_MONTH] = 1
        eventsCalendar.addEvent(c)

        selected.setOnClickListener {
            val dates = eventsCalendar.getDatesFromSelectedRange()
            Log.e("SELECTED SIZE", dates.size.toString())
        }

        selected.typeface = FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this)

        val dc = Calendar.getInstance()
        dc.add(Calendar.DAY_OF_MONTH, 2)
    }

    private fun getDateString(time: Long?): String {
        if (time != null) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = time
            val month = when (cal[Calendar.MONTH]) {
                Calendar.JANUARY -> "January"
                Calendar.FEBRUARY -> "February"
                Calendar.MARCH -> "March"
                Calendar.APRIL -> "April"
                Calendar.MAY -> "May"
                Calendar.JUNE -> "June"
                Calendar.JULY -> "July"
                Calendar.AUGUST -> "August"
                Calendar.SEPTEMBER -> "September"
                Calendar.OCTOBER -> "October"
                Calendar.NOVEMBER -> "November"
                Calendar.DECEMBER -> "December"
                else -> ""
            }
            return "$month ${cal[Calendar.DAY_OF_MONTH]}, ${cal[Calendar.YEAR]}"
        } else return ""
    }
}
