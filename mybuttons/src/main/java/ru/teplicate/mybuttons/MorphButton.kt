package ru.teplicate.mybuttons

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Button
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import ru.teplicate.mybuttons.Params.*


enum class State {
    FOLDED,
    UNFOLDED
}

enum class Params {
    WIDTH,
    HEIGHT,
    COLOR,
    MORPH_HEIGHT,
    MORPH_WIDTH,
    MORPH_RADIUS,
    ORDINATE_BOUNCING
}

class MorphButton : Button {
    private val className = this::class.java.name
    private var areParamInit = false
    private lateinit var sourceText: CharSequence
    private val paramsMap = HashMap<Params, Number>()
    private val foldingAnimatorSet by lazy { AnimatorSet() }
    private val unfoldingAnimatorSet by lazy { AnimatorSet() }
    private val bouncingAnimatorSet by lazy { AnimatorSet() }
    private val animationRestarter by lazy { countdownRestartAnim() }
    private var mState = State.FOLDED
    private lateinit var animatedDrawable: GradientDrawable

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getCustomAttributes(context, attrs)
        init()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getCustomAttributes(context, attrs)
        init()
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        getCustomAttributes(context, attrs)
        init()
    }

    private fun init() {
        val drawable = GradientDrawable().also {
            it.shape = GradientDrawable.RECTANGLE
            it.setColor(
                ContextCompat.getColor(
                    context,
                    paramsMap.getValue(COLOR).toInt()
                )
            )
        }
        animatedDrawable = drawable
        background = animatedDrawable
    }

    private fun initSourceSizes() {
        paramsMap[HEIGHT] = this.height
        paramsMap[WIDTH] = this.width
        sourceText = this.text
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!areParamInit) {
            initSourceSizes()
            setFoldAnimator()
            areParamInit = true
        }
    }

    private fun setFoldAnimator() {
        foldingAnimatorSet.playTogether(
            getAnimationRadius(
                sourceRadius = 0F,
                targetRadius = paramsMap.getValue(MORPH_RADIUS).toFloat()
            ),
            getAnimationWidth(
                sourceWidth = paramsMap.getValue(WIDTH).toInt(),
                targetWidth = paramsMap.getValue(MORPH_WIDTH).toInt()
            ),
            getAnimationHeight(
                sourceHeight = paramsMap.getValue(HEIGHT).toInt(),
                targetHeight = paramsMap.getValue(MORPH_HEIGHT).toInt()
            )
        )

        foldingAnimatorSet.doOnEnd {
            mState = State.FOLDED
            startBouncingAnimation()
        }

        foldingAnimatorSet.startDelay = 500
        foldingAnimatorSet.start()
    }

    private fun setUnfoldAnimator() {
        bouncingAnimatorSet.end()
        unfoldingAnimatorSet.playTogether(
            getAnimationRadius(
                sourceRadius = paramsMap.getValue(MORPH_RADIUS).toFloat(),
                targetRadius = 0F
            ),
            getAnimationWidth(
                sourceWidth = paramsMap.getValue(MORPH_WIDTH).toInt(),
                targetWidth = paramsMap.getValue(WIDTH).toInt()
            ),
            getAnimationHeight(
                sourceHeight = paramsMap.getValue(MORPH_HEIGHT).toInt(),
                targetHeight = paramsMap.getValue(HEIGHT).toInt()
            )
        )

        unfoldingAnimatorSet.doOnEnd {
            Log.i(className, "folding ended")
            isClickable = true
        }
        unfoldingAnimatorSet.start()
    }

    private fun startBouncingAnimation() {
        bouncingAnimatorSet.play(getOrdinateAnimator())
        bouncingAnimatorSet.doOnEnd {
            Log.i(className, "bouncing ended")
            mState = State.UNFOLDED
        }
        bouncingAnimatorSet.start()
    }

    private fun getCustomAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MorphButton, 0, 0)
        val color = a.getResourceId(R.styleable.MorphButton_morph_button_color, 0)
        val morphWidth = a.getDimension(R.styleable.MorphButton_morph_width, 100F)
        val morphHeight = a.getDimension(R.styleable.MorphButton_morph_height, 100F)
        val morphRadius = a.getDimension(R.styleable.MorphButton_morph_radius, 50F)
        val foldingDuration = a.getInt(R.styleable.MorphButton_folding_duration, 1000).toLong()
        val unfoldingDuration = a.getInt(R.styleable.MorphButton_unfolding_duration, 1000).toLong()
        val bouncingDuration = a.getInt(R.styleable.MorphButton_bouncing_duration, 1500).toLong()
        val ordinateBouncing = a.getInt(R.styleable.MorphButton_ordinate_bouncing, 50)

        paramsMap[COLOR] = color
        paramsMap[MORPH_WIDTH] = morphWidth
        paramsMap[MORPH_HEIGHT] = morphHeight
        paramsMap[MORPH_RADIUS] = morphRadius
        paramsMap[ORDINATE_BOUNCING] = ordinateBouncing
        foldingAnimatorSet.duration = foldingDuration
        unfoldingAnimatorSet.duration = unfoldingDuration
        bouncingAnimatorSet.duration = bouncingDuration

        a.recycle()
    }

    private fun getOrdinateAnimator(): ObjectAnimator {
        return ObjectAnimator.ofFloat(
            this,
            "y",
            this.y,
            this.y + paramsMap.getValue(ORDINATE_BOUNCING).toInt(),
            this.y
        )
            .also {
                it.repeatCount = Animation.INFINITE
            }
    }

    private fun getAnimationRadius(sourceRadius: Float, targetRadius: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(animatedDrawable, "cornerRadius", sourceRadius, targetRadius)
    }

    private fun getAnimationHeight(sourceHeight: Int, targetHeight: Int): ValueAnimator {
        val heightAnimation = ValueAnimator.ofInt(sourceHeight, targetHeight)
        heightAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams as ViewGroup.LayoutParams?
            layoutParams?.let {
                layoutParams.height = `val`
                this.layoutParams = layoutParams
            }
        }

        return heightAnimation
    }

    private fun getAnimationWidth(sourceWidth: Int, targetWidth: Int): ValueAnimator {
        val heightAnimation = ValueAnimator.ofInt(sourceWidth, targetWidth)
        heightAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = layoutParams as ViewGroup.LayoutParams?
            layoutParams?.let {
                layoutParams.width = `val`
                this.layoutParams = layoutParams
            }
        }

        return heightAnimation
    }

    override fun performClick(): Boolean {
        return if (mState == State.UNFOLDED)
            return super.performClick()
        else {
            setUnfoldAnimator()
            animationRestarter.start()
            false
        }
    }

    private fun countdownRestartAnim(): CountDownTimer {
        return object : CountDownTimer(5000, 1000) {
            override fun onFinish() {
                mState = State.FOLDED
                foldingAnimatorSet.start()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
    }
}