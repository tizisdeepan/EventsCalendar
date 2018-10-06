package com.events.calendar.views

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup

class MonthGridContainer : ViewGroup {

    lateinit var mDatesGridLayout: DatesGridLayout
    lateinit var mContext: Context

    constructor(context: Context) : super(context) {}

    constructor(context: Context, datesGridLayout: DatesGridLayout) : super(context) {
        mContext = context
        mDatesGridLayout = datesGridLayout
        addView(mDatesGridLayout)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), View.MeasureSpec.AT_MOST)
        mDatesGridLayout.measure(widthMeasureSpec, heightMeasureSpec)
        val height = mDatesGridLayout.measuredHeight
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mDatesGridLayout.layout(l, 0, r, b)
    }
}
