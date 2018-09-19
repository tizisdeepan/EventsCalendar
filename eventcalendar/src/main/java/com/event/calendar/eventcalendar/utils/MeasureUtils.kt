package com.event.calendar.eventcalendar.utils

import android.view.View

object MeasureUtils {
    fun getMeasurement(measureSpec: Int, contentSize: Int): Int {
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        var resultSize = 0
        when (specMode) {
            View.MeasureSpec.UNSPECIFIED -> resultSize = contentSize
            View.MeasureSpec.AT_MOST -> resultSize = Math.min(specSize, contentSize)
            View.MeasureSpec.EXACTLY -> resultSize = specSize
        }
        return resultSize
    }
}
