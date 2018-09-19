package com.event.calendar.eventcalendar.views

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.OverScroller
import com.event.calendar.eventcalendar.R

class VerticalSlidingViewUsingGesture : LinearLayout {
    private var mContext: Context? = null
    private var mAttrs: AttributeSet? = null
    private var mDefStyleAttrs: Int = 0
    private var mDefStyleRes: Int = 0
    private var mEventCalendar: EventCalendar? = null
    private var mSlidingLayout: View? = null
    private var mGestureDetector: GestureDetector? = null
    private var mInitialX: Float = 0.toFloat()
    private var mInitialY: Float = 0.toFloat()
    private var mTouchSlop: Int = 0
    private var mSlidingLayoutTranslationY: Float = 0.toFloat()
    private var mResources: Resources? = null
    private var mSlidingMinTop: Float = 0.toFloat()
    private var mSlidingMaxTop: Int = 0
    private var mMinSlidingTransY: Float = 0.toFloat()
    private var mSlidingLayoutParams: ViewGroup.LayoutParams? = null
    private var mScroller: OverScroller? = null

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (!mScroller!!.isFinished) {
                mScroller!!.abortAnimation()
            }
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            fling(velocityY)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scrollChildViewsBy(distanceY)
            return true
        }

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
        mResources = mContext!!.resources

        this.orientation = LinearLayout.VERTICAL

        mGestureDetector = GestureDetector(mContext, mGestureListener)
        mTouchSlop = ViewConfiguration.get(mContext).scaledEdgeSlop / 2
        mScroller = OverScroller(mContext)
        mSlidingLayoutTranslationY = 0f
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mEventCalendar = getChildAt(0) as EventCalendar
        mSlidingLayout = getChildAt(1)
        mSlidingLayoutParams = mSlidingLayout!!.layoutParams
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mSlidingMaxTop = mSlidingLayout!!.top
        mSlidingMinTop = top + (mResources!!.getDimension(R.dimen.dimen_date_text_view) + mResources!!.getDimension(R.dimen.height_week_day_header))
        mMinSlidingTransY = mSlidingMinTop - mSlidingMaxTop
        mSlidingLayout!!.layoutParams.height = (mSlidingLayout!!.measuredHeight + -mMinSlidingTransY).toInt()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialX = event.x
                mInitialY = event.y
                mGestureDetector!!.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val yDiff = Math.abs(y - mInitialY).toInt()
                val xDiff = Math.abs(x - mInitialX).toInt()

                if (yDiff > mTouchSlop * 3 || yDiff > xDiff) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return mGestureDetector!!.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset()) {
            val oldY = mSlidingLayout!!.y.toInt()
            val y = mScroller!!.currY
            val dy = oldY - y
            scrollChildViewsBy(dy.toFloat())
        }
        super.computeScroll()
    }

    private fun scrollChildViewsBy(distanceY: Float) {
        val transY = -distanceY
        if (mSlidingLayoutTranslationY + transY > mMinSlidingTransY && mSlidingLayoutTranslationY + transY <= 0) {
            mSlidingLayoutTranslationY += transY
        } else if (mSlidingLayoutTranslationY + transY > mMinSlidingTransY) {
            mSlidingLayoutTranslationY = 0f
        } else {
            mSlidingLayoutTranslationY = mMinSlidingTransY
        }
        mSlidingLayout!!.translationY = mSlidingLayoutTranslationY
        if (mSlidingLayoutTranslationY == 0f) {
            mEventCalendar!!.setCurrentMonthTranslationFraction(0f)
        } else {
            mEventCalendar!!.setCurrentMonthTranslationFraction(-mSlidingLayoutTranslationY / mMinSlidingTransY)
        }
    }

    private fun fling(velocityY: Float) {
        mScroller!!.fling(mSlidingLayout!!.x.toInt(), mSlidingLayout!!.y.toInt(), 0, velocityY.toInt(), 0, 0, mSlidingMinTop.toInt(), mSlidingMaxTop)
    }
}
