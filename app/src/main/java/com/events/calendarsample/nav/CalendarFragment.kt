package com.events.calendarsample.nav


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.events.calendar.views.EventsCalendar
import com.events.calendarsample.FontsManager

import com.events.calendarsample.R
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class CalendarFragment : Fragment(), EventsCalendar.Callback {
    override fun onDaySelected(selectedDate: Calendar?) {

    }

    override fun onDayLongPressed(selectedDate: Calendar?) {

    }

    override fun onMonthChanged(monthStartDate: Calendar?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val today = Calendar.getInstance()

        val start = Calendar.getInstance()
        start.add(Calendar.YEAR, -10)

        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 20)

        eventsCalendar.setSelectionMode(eventsCalendar.SINGLE_SELECTION)
                .setToday(today)
                .setMonthRange(today, end)
                .setWeekStartDay(Calendar.SUNDAY, false)
                .setDatesTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_REGULAR, requireContext()))
                .setMonthTitleTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, requireContext()))
                .setWeekHeaderTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, requireContext()))
                .setCallback(this)
                .build()

        eventsCalendar.post {
            eventsCalendar.setCurrentSelectedDate(today)
        }

        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, 2)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 3)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 4)
        eventsCalendar.addEvent(c)
        c.add(Calendar.DAY_OF_MONTH, 7)
        eventsCalendar.addEvent(c)
    }
}
