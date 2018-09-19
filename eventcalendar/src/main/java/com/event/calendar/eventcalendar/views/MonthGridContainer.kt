package com.event.calendar.eventcalendar.views

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup

class MonthGridContainer : ViewGroup {

    private var mMonthDatesGridLayout: MonthDatesGridLayout? = null
    private var mContext: Context? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, monthDatesGridLayout: MonthDatesGridLayout) : super(context) {
        mContext = context
        mMonthDatesGridLayout = monthDatesGridLayout
        addView(mMonthDatesGridLayout)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var hms = heightMeasureSpec
        if (mContext?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) hms = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(hms), View.MeasureSpec.AT_MOST)
        mMonthDatesGridLayout?.measure(widthMeasureSpec, hms)
        val height = mMonthDatesGridLayout?.measuredHeight!!
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mMonthDatesGridLayout?.layout(l, 0, r, b)
    }
}
