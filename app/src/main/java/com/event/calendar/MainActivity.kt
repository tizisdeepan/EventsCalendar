package com.event.calendar

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.event.calendar.eventcalendar.views.EventCalendarCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), EventCalendarCallback {

    override fun onDaySelected(date: Calendar?, isClick: Boolean) {
        if (date?.timeInMillis!! < calendar_view_pager.minDate.timeInMillis) {

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
        calendar_view_pager.setToday(today)
        calendar_view_pager.setMonthRange(today, end)
        calendar_view_pager.setWeekStartDay(Calendar.SUNDAY, false)
        calendar_view_pager.setCurrentSelectedDate(today)
        calendar_view_pager.setEventDotColor(Color.parseColor("#FF0000"))
        calendar_view_pager.invalidateColors()
        calendar_view_pager.setDatesTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_REGULAR, this))
        calendar_view_pager.setMonthTitleTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        calendar_view_pager.setWeekHeaderTypeface(FontsManager.getTypeface(FontsManager.OPENSANS_SEMIBOLD, this))
        calendar_view_pager.setCallback(this)

        text.setOnClickListener {
            calendar_view_pager.nextPage(true)
        }
    }
}
