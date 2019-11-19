package ru.teplicate.mybuttons

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import kotlin.math.abs

class SwipeableButton : Button {
    private var startPoint: Float = 0F
    private var prevX: Float = 0F
    private var beyondEdge: Boolean = false
    private var startAlpha: Int = 0
    private var leftDistance = 0F
    private var rightDistance = 0F
    private lateinit var onSwipeListener: OnSwipeListener

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        return event?.let {
            when (event.action) {

                //Pressing button
                MotionEvent.ACTION_DOWN -> {
                    presetTrackers(event.x)
                    startPoint = event.x
                    startAlpha = background.alpha
                    prevX = startPoint
                    true
                }
                //releasing button
                MotionEvent.ACTION_UP -> {
                    if (beyondEdge)
                        onSwipeListener.onSwipe(view = this)
                    resetBackgroundState()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    beyondEdge = event.x <= 0 || event.x >= this.width
                    changeBackground(event.x)
                    true
                }
                else -> false
            }
        } ?: false
    }


    private fun presetTrackers(mX: Float) {
        startPoint = mX
        startAlpha = background.alpha
        prevX = startPoint
        leftDistance = if (this.x == 0F) {
            startPoint + startPoint * 0.2F
        } else {
            abs(startPoint) + abs(startPoint) * 0.2F
        }
        rightDistance =
            abs(startPoint - (this.width)) + abs(startPoint - (this.width)) * 0.2F
    }

    private fun resetBackgroundState() {
        background.alpha = startAlpha
        beyondEdge = false
        background.invalidateSelf()
    }

    private fun changeBackground(mx: Float) {
        val rightDirection = mx > prevX
        val shiftProportion = if (!rightDirection) {
            if (beyondEdge) {
                0.2F
            } else calcCurrentShiftProportion(mx, leftDistance)

        } else {
            if (beyondEdge) {
                0.2F
            } else calcCurrentShiftProportion(mx, rightDistance)
        }

        prevX = mx
        background.alpha = (startAlpha * shiftProportion).toInt()
        background.invalidateSelf()
    }

    private fun calcCurrentShiftProportion(mx: Float, distance: Float): Float {
        return 1F - abs(mx - startPoint) / distance
    }

    fun setupOnSwipeListener(l: OnSwipeListener): View {
        this.onSwipeListener = l

        return this
    }
}

interface OnSwipeListener {
    fun onSwipe(view: View)
}