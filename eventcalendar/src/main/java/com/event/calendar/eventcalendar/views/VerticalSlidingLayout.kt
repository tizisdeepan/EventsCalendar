package com.event.calendar.eventcalendar.views

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import com.event.calendar.eventcalendar.R
import com.event.calendar.eventcalendar.utils.ZMailCalendarUtil

class VerticalSlidingLayout : ViewGroup {

    lateinit var mContext: Context
    private var mAttrs: AttributeSet? = null
    private var mDefStyleAttrs: Int = 0
    private var mDefStyleRes: Int = 0
    private var mViewDragHelper: ViewDragHelper? = null
    private var mEventCalendar: EventCalendar? = null   //VIEW PAGER -Horizontally Sliding MonthViews and WeekViews
    private var mSlidingLayout: View? = null                    //LIST VIEW
    private var mIntermediateView: FrameLayout? = null          //VIEW BEING DRAGGED , mEventCalendar and mSlidingLayout will be dragged along
    private var mResources: Resources? = null

    private var mSlidingMaxTop: Float = 0.toFloat()
    private var mSlidingMinTop: Float = 0.toFloat()
    private var mSlidingDistance: Float = 0.toFloat()
    private var mSlidingTranslationY: Float = 0.toFloat()
    private var mTouchSlop: Int = 0
    private var mInitialX: Float = 0.toFloat()
    private var mInitialY: Float = 0.toFloat()
    private var mCallback: Callback? = null

    /**
     * Returns the current mode constant of EventCalendar
     */
    /**
     * sets current mode constant of EventCalendar
     * Called when dragged child view has been released VerticalDragCallback.onViewReleased()
     *
     * @param mode constant specifying current mode
     */
    private var calendarMode: Int
        get() = ZMailCalendarUtil.currentMode
        set(mode) {
            if (ZMailCalendarUtil.currentMode !== mode) {
                mEventCalendar?.setCalendarMode(mode)
            }
        }

    interface Callback {
        /**
         * @return true if the sliding view can be dragged
         */
        fun canDragViews(): Boolean
    }

    constructor(context: Context) : super(context) {
        init(context, null, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, -1, -1)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, -1)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        mContext = context
        mAttrs = attrs
        mDefStyleAttrs = defStyleAttr
        mDefStyleRes = defStyleRes

        mResources = mContext.resources

        mSlidingMaxTop = 0f
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mEventCalendar = getChildAt(0) as EventCalendar
        mSlidingLayout = getChildAt(1)
        mIntermediateView = FrameLayout(mContext)
        mIntermediateView?.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(mIntermediateView)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, VerticalDragCallback())
        mTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val slidingLayoutHeight = (totalHeight - mEventCalendar!!.visibleContentHeight).toInt()

        measureChild(mSlidingLayout, widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(slidingLayoutHeight, View.MeasureSpec.EXACTLY))
        measureChild(mEventCalendar, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mEventCalendar?.layout(0, 0, r, mEventCalendar!!.measuredHeight)

        val viewPagerHeight = mEventCalendar!!.measuredHeight

        mSlidingMinTop = -(viewPagerHeight - mEventCalendar!!.visibleContentHeight)
        mSlidingMaxTop = 0f
        mSlidingDistance = mSlidingMaxTop - mSlidingMinTop
        mSlidingTranslationY = 0f

        val bottom = measuredHeight
        mSlidingLayout!!.layout(0, viewPagerHeight, r, (bottom + mSlidingDistance).toInt())

        mIntermediateView!!.layout(0, 0, r, (bottom + mSlidingDistance).toInt())

        if (ZMailCalendarUtil.WEEK_MODE == calendarMode) {
            scrollChildViewsBy(-mSlidingDistance)
            mIntermediateView!!.offsetTopAndBottom((-mSlidingDistance).toInt())
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN && mEventCalendar!!.isPagingEnabled) {
            mViewDragHelper!!.shouldInterceptTouchEvent(ev)
        }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = ev.x
                mInitialY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(mInitialX - ev.x).toInt()
                val dy = Math.abs(mInitialY - ev.y).toInt()
                if (dy > mTouchSlop && dy > dx) {
                    if (ZMailCalendarUtil.currentMode == ZMailCalendarUtil.WEEK_MODE) {
                        var canDragViews: Boolean? = false
                        if (mInitialY - ev.y < 0) {
                            canDragViews = mCallback?.canDragViews()
                        }
                        return canDragViews ?: false || isPointingWeekViewpager(mInitialY.toInt())
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            mViewDragHelper!!.processTouchEvent(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mViewDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * called to vertically drag mSlidingLayout and mEventCalendar by dY
     *
     * @param dy vertical drag offset
     */
    private fun scrollChildViewsBy(dy: Float) {
        if (mSlidingTranslationY + dy > mSlidingMinTop && mSlidingTranslationY + dy <= 0) {
            mSlidingTranslationY += dy
        } else if (mSlidingTranslationY + dy > 0) {
            mSlidingTranslationY = 0f
        } else {
            mSlidingTranslationY = mSlidingMinTop
        }
        mSlidingLayout!!.translationY = mSlidingTranslationY
        if (mSlidingTranslationY == 0f) {
            mEventCalendar!!.setCurrentMonthTranslationFraction(0f)
        } else {
            mEventCalendar!!.setCurrentMonthTranslationFraction(mSlidingTranslationY / mSlidingDistance)
        }
    }

    /**
     * To check whether pointer points the EventCalendar
     *
     * @param y y-value of pointer
     * @return true if y is in vertical span of EventCalendar
     */
    private fun isPointingWeekViewpager(y: Int): Boolean {
        return y < mResources!!.getDimension(R.dimen.dimen_date_text_view) + mResources!!.getDimension(R.dimen.height_week_day_header) && y > mEventCalendar!!.top
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    /**
     * ViewDragHelper Callbacks
     */
    private inner class VerticalDragCallback : ViewDragHelper.Callback() {

        internal var isModeSet = false
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if (top < mSlidingMaxTop && top > mSlidingMinTop) {
                top
            } else if (top > mSlidingMaxTop) {
                mSlidingMaxTop.toInt()
            } else {
                mSlidingMinTop.toInt()
            }
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            scrollChildViewsBy(dy.toFloat())
        }

        override fun getOrderedChildIndex(index: Int): Int {
            return 2
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return (mSlidingDistance / 2).toInt()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val minVelocity = mViewDragHelper!!.minVelocity
            if (Math.abs(yvel) < minVelocity) {
                if (mSlidingLayout!!.y < mSlidingLayout!!.top - mSlidingDistance / 2) {
                    mViewDragHelper!!.settleCapturedViewAt(0, mSlidingMinTop.toInt())
                } else {
                    mViewDragHelper!!.settleCapturedViewAt(0, 0)
                }
                if (!isModeSet) {
                    setCalendarModeBasedOnTop()
                }
            }
            if (yvel > minVelocity) {
                mViewDragHelper!!.settleCapturedViewAt(0, 0)
                calendarMode = ZMailCalendarUtil.MONTH_MODE
            } else if (yvel < -minVelocity) {
                mViewDragHelper!!.settleCapturedViewAt(0, mSlidingMinTop.toInt())
                calendarMode = ZMailCalendarUtil.WEEK_MODE
            }
            invalidate()
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (state == ViewDragHelper.STATE_DRAGGING) {
                isModeSet = false
                mEventCalendar!!.isPagingEnabled = false
            }
            if (state == ViewDragHelper.STATE_IDLE) {
                if (!isModeSet) {
                    setCalendarModeBasedOnTop()
                }
                mEventCalendar!!.isPagingEnabled = true
                mEventCalendar!!.changeAdapter()
            }
        }

        private fun setCalendarModeBasedOnTop() {
            if (mSlidingLayout!!.y < mSlidingLayout!!.top - mSlidingDistance / 2) {
                calendarMode = ZMailCalendarUtil.WEEK_MODE
            } else {
                calendarMode = ZMailCalendarUtil.MONTH_MODE
            }
            isModeSet = true
        }
    }
}
