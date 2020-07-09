package p2pdops.dopsender.views

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import p2pdops.dopsender.R
import java.util.*

class ProgressBackground : RelativeLayout {
    private var rippleColor = 0
    private var rippleStrokeWidth = 0f
    private var rippleRadius = 0f
    private var rippleDurationTime = 0
    private var rippleAmount = 0
    private var rippleDelay = 0
    private var rippleScale = 0f
    private var rippleEndAlpha = 0f
    private var rippleType = 0
    private var rippleMode = 0
    private var paint: Paint? = null
    var isRippleAnimationRunning = false
        private set
    private var animatorSet: AnimatorSet? = null
    private var animatorList: ArrayList<Animator>? = null
    private var rippleParams: LayoutParams? = null
    private val rippleViewList =
        ArrayList<RippleView>()

    constructor(context: Context?) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        if (isInEditMode) return
        requireNotNull(attrs) { "Attributes should be provided to this view," }
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ProgressBackground)
        rippleColor = typedArray.getColor(
            R.styleable.ProgressBackground_progress_color,
            resources.getColor(R.color.rippelColor)
        )
        rippleStrokeWidth = typedArray.getDimension(
            R.styleable.ProgressBackground_ripple_width,
            resources.getDimension(R.dimen.rippleStrokeWidth)
        )
        rippleRadius = typedArray.getDimension(
            R.styleable.ProgressBackground_rb_radius,
            resources.getDimension(R.dimen.rippleRadius)
        )

        rippleDurationTime = typedArray.getInt(
            R.styleable.ProgressBackground_rb_duration,
            DEFAULT_DURATION_TIME
        )
        rippleAmount = typedArray.getInt(
            R.styleable.ProgressBackground_rb_rippleAmount,
            DEFAULT_RIPPLE_COUNT
        )
        rippleScale = typedArray.getFloat(
            R.styleable.ProgressBackground_rb_scale,
            DEFAULT_SCALE
        )

        rippleEndAlpha = typedArray.getFloat(
            R.styleable.ProgressBackground_rb_end_alpha,
            DEFAULT_END_ALPHA
        )
        rippleType = typedArray.getInt(
            R.styleable.ProgressBackground_rb_type,
            DEFAULT_FILL_TYPE
        )

        rippleMode = typedArray.getInt(
            R.styleable.ProgressBackground_rb_mode,
            DEFAULT_FILL_MODE
        )

        rippleMode = typedArray.getInt(
            R.styleable.ProgressBackground_rb_mode,
            DEFAULT_FILL_MODE
        )
        typedArray.recycle()
        rippleDelay = rippleDurationTime / rippleAmount
        paint = Paint()
        paint!!.isAntiAlias = true
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0f
            paint!!.style = Paint.Style.FILL
        } else paint!!.style = Paint.Style.STROKE
        paint!!.color = rippleColor
        rippleParams = LayoutParams(
            (2 * (rippleRadius + rippleStrokeWidth)).toInt(),
            (2 * (rippleRadius + rippleStrokeWidth)).toInt()
        )
        rippleParams!!.addRule(
            when (rippleMode) {
                1 -> ALIGN_PARENT_START
                -1 -> ALIGN_PARENT_END
                else -> CENTER_IN_PARENT
            },
            TRUE
        )
        animatorSet = AnimatorSet()
        animatorSet!!.interpolator = AccelerateDecelerateInterpolator()
        animatorList = ArrayList()
        for (i in 0 until rippleAmount) {
            val rippleView = RippleView(getContext())
            addView(rippleView, rippleParams)
            rippleViewList.add(rippleView)
            val scaleXAnimator =
                ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale)
            scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleXAnimator.repeatMode = ObjectAnimator.RESTART
            scaleXAnimator.startDelay = i * rippleDelay.toLong()
            scaleXAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(scaleXAnimator)
            val scaleYAnimator =
                ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale)
            scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleYAnimator.repeatMode = ObjectAnimator.RESTART
            scaleYAnimator.startDelay = i * rippleDelay.toLong()
            scaleYAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(scaleYAnimator)
            val alphaAnimator =
                ObjectAnimator.ofFloat(
                    rippleView,
                    "Alpha",
                    1.0f,
//                    if (i % 2 == 0)
                    rippleEndAlpha
//                    else rippleEndAlpha
                )
            alphaAnimator.repeatCount = ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = ObjectAnimator.RESTART
            alphaAnimator.startDelay = i * rippleDelay.toLong()
            alphaAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(alphaAnimator)
        }
        animatorSet!!.playTogether(animatorList)
    }

    private inner class RippleView(context: Context?) :
        View(context) {
        override fun onDraw(canvas: Canvas) {
            val radius = width.coerceAtMost(height) / 2
            canvas.drawCircle(
                radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth,
                paint!!
            )
        }

        init {
            this.visibility = INVISIBLE
        }
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning) {
            for (rippleView in rippleViewList) {
                rippleView.visibility = View.VISIBLE
            }
            animatorSet!!.start()
            isRippleAnimationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning) {
            animatorSet!!.end()
            isRippleAnimationRunning = false
        }
    }

    companion object {
        private const val DEFAULT_RIPPLE_COUNT = 6
        private const val DEFAULT_DURATION_TIME = 3000
        private const val DEFAULT_SCALE = 6.0f
        private const val DEFAULT_FILL_TYPE = 0
        private const val DEFAULT_FILL_MODE = 0
        private const val DEFAULT_END_ALPHA = 0.0f
    }
}