package com.events.calendar.utils

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.Log
import android.util.MonthDisplayHelper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet
import kotlin.math.ceil
import android.util.DisplayMetrics


object EventsCalendarUtil {
    const val WEEK_MODE = 0
    const val MONTH_MODE = 1
    const val SINGLE_SELECTION = 0
    const val RANGE_SELECTION = 1
    const val MULTIPLE_SELECTION = 2
    private const val MONTHS_IN_YEAR = 12
    const val YYYY_MM_DD = 10
    const val DD_MM_YYYY = 20
    private const val MM_DD_YYYY = 30
    const val DISPLAY_STRING = 200
    const val YYYY_MM = 100
    var today: Calendar = Calendar.getInstance()

    var SELECTION_MODE = 0

    var currentMode = MONTH_MODE
    var weekStartDay = Calendar.MONDAY
    private var currentSelectedDate: Calendar = Calendar.getInstance()
    var tobeSelectedDate = 1
    const val DEFAULT_NO_OF_MONTHS = 480
    var dateTextFontSize: Float = 0f
    var weekHeaderFontSize: Float = 0f
    var monthTitleFontSize: Float = 0f
    var primaryTextColor: Int = 0
    var secondaryTextColor: Int = 0
    var selectedTextColor: Int = 0
    var selectionColor: Int = 0
    var rangeSelectionColor: Int = 0
    var rangeSelectionStartColor: Int = 0
    var rangeSelectionEndColor: Int = 0
    var eventDotColor: Int = 0
    var monthTitleColor: Int = 0
    var weekHeaderColor: Int = 0
    var selectedDate: Calendar = Calendar.getInstance()
    var monthPos = 0
    var datesTypeface: Typeface? = null
    var monthTitleTypeface: Typeface? = null
    var weekHeaderTypeface: Typeface? = null
    var isBoldTextOnSelectionEnabled: Boolean = false

    val datesInSelectedRange: LinkedHashMap<String, Calendar> = LinkedHashMap()
    private var minDateInRange: Calendar? = null
    private var maxDateInRange: Calendar? = null

    fun updateSelectedDates(c: Calendar) {
        if (datesInSelectedRange.contains(getDateString(c, DD_MM_YYYY))) datesInSelectedRange.remove(getDateString(c, DD_MM_YYYY))
        else datesInSelectedRange[getDateString(c, DD_MM_YYYY)] = c
        Log.e("SIZE", datesInSelectedRange.size.toString())
    }

    fun updateMinMaxDateInRange(c: Calendar) {
        when {
            areDatesSame(minDateInRange, c) -> {
                minDateInRange = null
                maxDateInRange = null
            }
            areDatesSame(maxDateInRange, c) -> {
                minDateInRange = null
                maxDateInRange = null
            }
            c.before(minDateInRange) || areDatesSame(c, minDateInRange) || minDateInRange == null -> {
                minDateInRange = c
                maxDateInRange = null
            }
            else -> maxDateInRange = c
        }
        refreshRange()
    }

    private fun refreshRange() {
        if (minDateInRange == null || maxDateInRange == null) datesInSelectedRange.clear()
        else {
            if (minDateInRange != null && maxDateInRange != null) {
                val min = minDateInRange!!.clone() as Calendar
                val max = maxDateInRange!!.clone() as Calendar
                datesInSelectedRange.clear()
                while (min.before(max) || areDatesSame(min, max)) {
                    datesInSelectedRange[getDateString(min, DD_MM_YYYY)] = min
                    min.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else datesInSelectedRange.clear()
        }
    }

    val disabledDates: ArrayList<Calendar> = ArrayList()
    val disabledDays: LinkedHashSet<Int> = LinkedHashSet()

    fun areDatesSame(date1: Calendar?, date2: Calendar?): Boolean = date1 != null && date2 != null && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) && date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)

    fun areMonthsSame(date1: Calendar?, date2: Calendar?): Boolean = date1 != null && date2 != null && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)

    fun isPastDay(date: Calendar): Boolean {
        if (date.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return true
        else if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            if (date.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return true
            else if (date.get(Calendar.MONTH) == today.get(Calendar.MONTH)) if (date.get(Calendar.DATE) < today.get(Calendar.DATE)) return true
        }
        return false
    }

    fun isToday(date: Calendar): Boolean = areDatesSame(today, date)

    fun isFutureDay(date: Calendar): Boolean {
        if (date.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true
        else if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            if (date.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return true
            else if (date.get(Calendar.MONTH) == today.get(Calendar.MONTH)) if (date.get(Calendar.DATE) > today.get(Calendar.DATE)) return true
        }
        return false
    }

    fun setCurrentSelectedDate(selectedDate: Calendar): Boolean {
        if (!areDatesSame(currentSelectedDate, selectedDate)) {
            currentSelectedDate = selectedDate.clone() as Calendar
            return true
        }
        return false
    }

    fun getMonthCount(startMonth: Calendar, endMonth: Calendar): Int {
        val diffYear = endMonth.get(Calendar.YEAR) - startMonth.get(Calendar.YEAR)
        val diffMonth = endMonth.get(Calendar.MONTH) - startMonth.get(Calendar.MONTH)
        return diffMonth + MONTHS_IN_YEAR * diffYear + 1
    }

    fun getWeekCount(startDay: Calendar, endDay: Calendar): Int {
        var noOfWeeks = 0
        var finished = false
        val helper = MonthDisplayHelper(startDay.get(Calendar.YEAR), startDay.get(Calendar.MONTH), weekStartDay)
        while (!finished) {
            noOfWeeks += ceil(((helper.offset + helper.numberOfDaysInMonth).toFloat() / 7.0f).toDouble()).toInt()
            if (endDay.get(Calendar.MONTH) == helper.month && endDay.get(Calendar.YEAR) == helper.year) finished = true
            helper.nextMonth()
        }
        return noOfWeeks
    }

    fun getMonthPositionForDay(day: Calendar?, minDate: Calendar): Int {
        if (day == null) return -1
        val yearOffset = day.get(Calendar.YEAR) - minDate.get(Calendar.YEAR)
        val monthOffset = day.get(Calendar.MONTH) - minDate.get(Calendar.MONTH)
        return yearOffset * MONTHS_IN_YEAR + monthOffset
    }

    fun getMonthForWeekPosition(startMonth: Calendar, position: Int): Calendar {
        val helper = MonthDisplayHelper(startMonth.get(Calendar.YEAR), startMonth.get(Calendar.MONTH), weekStartDay)
        var finished = false
        var noOfWeeks = 0
        var offsetForPreviousMonth: Int
        val month = Calendar.getInstance()
        while (!finished) {
            offsetForPreviousMonth = ceil(((helper.offset + helper.numberOfDaysInMonth).toFloat() / 7.0f).toDouble()).toInt()
            noOfWeeks += offsetForPreviousMonth
            if (position + 1 <= noOfWeeks) {
                month.set(helper.year, helper.month, 1)
                finished = true
            }
            helper.nextMonth()
        }
        return month
    }

    fun getWeekNo(startMonth: Calendar, position: Int): Int {
        val helper = MonthDisplayHelper(startMonth.get(Calendar.YEAR), startMonth.get(Calendar.MONTH), weekStartDay)
        var finished = false
        var noOfWeeks = 0
        var offsetForPreviousMonth = 0
        while (!finished) {
            offsetForPreviousMonth = ceil(((helper.offset + helper.numberOfDaysInMonth).toFloat() / 7.0f).toDouble()).toInt()
            noOfWeeks += offsetForPreviousMonth
            if (position + 1 <= noOfWeeks) finished = true
            helper.nextMonth()
        }
        return offsetForPreviousMonth - (noOfWeeks - position) + 1
    }

    fun getWeekPosition(day: Calendar?, startMonth: Calendar): Int {
        val helper = MonthDisplayHelper(startMonth.get(Calendar.YEAR), startMonth.get(Calendar.MONTH), weekStartDay)
        var finished = false
        var noOfWeeks = 0
        while (!finished) {
            if (helper.month == day?.get(Calendar.MONTH) && helper.year == day.get(Calendar.YEAR)) {
                noOfWeeks += helper.getRowOf(day.get(Calendar.DATE))
                finished = true
            } else noOfWeeks += ceil(((helper.offset + helper.numberOfDaysInMonth).toFloat() / 7.0f).toDouble()).toInt()
            helper.nextMonth()
        }
        return noOfWeeks
    }

    fun getCurrentSelectedDate(): Calendar = currentSelectedDate

    fun getCalendar(dateStr: String, format: Int): Calendar {
        val calendar = Calendar.getInstance()
        val year: Int
        val month: Int
        val date: Int
        when (format) {
            YYYY_MM_DD -> {
                year = Integer.parseInt(dateStr.substring(0, dateStr.indexOf('/')))
                month = Integer.parseInt(dateStr.substring(dateStr.indexOf('/') + 1, dateStr.lastIndexOf('/'))) - 1
                date = Integer.parseInt(dateStr.substring(dateStr.lastIndexOf('/') + 1))
                calendar.set(year, month, date)
                return calendar
            }
            MM_DD_YYYY -> {
                month = Integer.parseInt(dateStr.substring(0, dateStr.indexOf('/'))) - 1
                date = Integer.parseInt(dateStr.substring(dateStr.indexOf('/') + 1, dateStr.lastIndexOf('/')))
                year = Integer.parseInt(dateStr.substring(dateStr.lastIndexOf('/') + 1))
                calendar.set(year, month, date)
                return calendar
            }
            else -> return Calendar.getInstance()
        }
    }

    fun getDateString(calendar: Calendar?, format: Int): String {
        val buffer = StringBuffer()
        if (calendar != null) {
            when (format) {
                YYYY_MM_DD -> {
                    buffer.append(calendar.get(Calendar.YEAR))
                    buffer.append("/")
                    val month = calendar.get(Calendar.MONTH) + 1
                    if (month < 10) buffer.append("0")
                    buffer.append(month)
                    buffer.append("/")
                    val date = calendar.get(Calendar.DATE)
                    if (date < 10) buffer.append("0")
                    buffer.append(date)
                    return buffer.toString()
                }
                DD_MM_YYYY -> {
                    buffer.append(calendar.get(Calendar.DATE))
                    buffer.append("/")
                    buffer.append(calendar.get(Calendar.MONTH) + 1)
                    buffer.append("/")
                    buffer.append(calendar.get(Calendar.YEAR))
                    return buffer.toString()
                }
                MM_DD_YYYY -> {
                    buffer.append(calendar.get(Calendar.MONTH) + 1)
                    buffer.append("/")
                    buffer.append(calendar.get(Calendar.DATE))
                    buffer.append("/")
                    buffer.append(calendar.get(Calendar.YEAR))
                    return buffer.toString()
                }
                else -> return "NULL"
            }
        } else return "NULL"
    }

    fun getMonthString(monthCalendar: Calendar, format: Int): String? {
        val buffer = StringBuffer()
        return when (format) {
            YYYY_MM -> {
                buffer.append(monthCalendar.get(Calendar.YEAR))
                buffer.append("/")
                val month = monthCalendar.get(Calendar.MONTH) + 1
                if (month < 10) buffer.append("0")
                buffer.append(month)
                buffer.toString()
            }
            DISPLAY_STRING -> DateFormat.format("MMMM yyyy", monthCalendar) as String
            else -> null
        }
    }

    fun convertPixelsToDp(px: Float, context: Context): Float = px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
