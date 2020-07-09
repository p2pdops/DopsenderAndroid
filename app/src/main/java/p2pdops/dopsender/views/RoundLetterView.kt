package p2pdops.dopsender.views

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import p2pdops.dopsender.R

class RoundLetterView : View {
    private var mTitleColor = DEFAULT_TITLE_COLOR
    private var mBackgroundColor = DEFAULT_BACKGROUND_COLOR
    private var mTitleText: String? = DEFAULT_TITLE
    private var mTitleSize = DEFAULT_TITLE_SIZE
    private var mTitleTextPaint: TextPaint? = null
    private var mBackgroundPaint: Paint? = null
    private var mInnerRectF: RectF? = null
    private var mViewSize = 0
    private var mFont = Typeface.defaultFromStyle(Typeface.BOLD)

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init(attrs, 0)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.RoundedLetterView, defStyle, 0
        )
        if (a.hasValue(R.styleable.RoundedLetterView_customTitle)) {
            mTitleText = a.getString(R.styleable.RoundedLetterView_customTitle)
        }
        mTitleColor = a.getColor(
            R.styleable.RoundedLetterView_customTitleColor,
            DEFAULT_TITLE_COLOR
        )
        mBackgroundColor = a.getColor(
            R.styleable.RoundedLetterView_customBackgroundColorValue,
            DEFAULT_BACKGROUND_COLOR
        )
        mTitleSize = a.getDimension(
            R.styleable.RoundedLetterView_customTitleSize,
            DEFAULT_TITLE_SIZE
        )
        a.recycle()

        //Title TextPaint
        mTitleTextPaint = TextPaint()
        mTitleTextPaint!!.flags = Paint.ANTI_ALIAS_FLAG
        mTitleTextPaint!!.typeface = mFont
        mTitleTextPaint!!.textAlign = Paint.Align.CENTER
        mTitleTextPaint!!.isLinearText = true
        mTitleTextPaint!!.color = mTitleColor
        mTitleTextPaint!!.textSize = mTitleSize

        //Background Paint
        mBackgroundPaint = Paint()
        mBackgroundPaint!!.flags = Paint.ANTI_ALIAS_FLAG
        mBackgroundPaint!!.style = Paint.Style.FILL
        mBackgroundPaint!!.color = mBackgroundColor
        mInnerRectF = RectF()
    }

    private fun invalidateTextPaints() {
        mTitleTextPaint!!.typeface = mFont
        mTitleTextPaint!!.textSize = mTitleSize
        mTitleTextPaint!!.color = mTitleColor
    }

    private fun invalidatePaints() {
        mBackgroundPaint!!.color = mBackgroundColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = resolveSize(
            DEFAULT_VIEW_SIZE,
            widthMeasureSpec
        )
        val height = resolveSize(
            DEFAULT_VIEW_SIZE,
            heightMeasureSpec
        )
        mViewSize = Math.min(width, height)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        mInnerRectF!![0f, 0f, mViewSize.toFloat()] = mViewSize.toFloat()
        mInnerRectF!!.offset(
            (width - mViewSize) / 2.toFloat(),
            (height - mViewSize) / 2.toFloat()
        )
        val centerX = mInnerRectF!!.centerX()
        val centerY = mInnerRectF!!.centerY()
        val xPos = centerX.toInt()
        val yPos =
            (centerY - (mTitleTextPaint!!.descent() + mTitleTextPaint!!.ascent()) / 2).toInt()
        canvas.drawOval(mInnerRectF!!, mBackgroundPaint!!)
        canvas.drawText(
            mTitleText!!,
            xPos.toFloat(),
            yPos.toFloat(),
            mTitleTextPaint!!
        )
    }

    /**
     * Gets the title string attribute value.
     * @return The title string attribute value.
     */
    /**
     * Sets the view's title string attribute value.
     * @param title The example string attribute value to use.
     */
    var titleText: String?
        get() = mTitleText
        set(title) {
            mTitleText = title
            invalidate()
        }

    /**
     * Gets the background color attribute value.
     * @return The background color attribute value.
     */
    fun getBackgroundColor(): Int {
        return mBackgroundColor
    }

    /**
     * Sets the view's background color attribute value.
     * @param backgroundColor The background color attribute value to use.
     */
    override fun setBackgroundColor(backgroundColor: Int) {
        mBackgroundColor = backgroundColor
        invalidatePaints()
    }

    /**
     * Gets the title size dimension attribute value.
     * @return The title size dimension attribute value.
     */
    /**
     * Sets the view's title size dimension attribute value.
     * @param titleSize The title size dimension attribute value to use.
     */
    var titleSize: Float
        get() = mTitleSize
        set(titleSize) {
            mTitleSize = titleSize
            invalidateTextPaints()
        }

    /**
     * Sets the view's title typeface.
     * @param font The typeface to be used for the text.
     */
    fun setTextTypeface(font: Typeface) {
        mFont = font
        invalidateTextPaints()
    }

    /**
     * Sets the view's title color attribute value.
     * @param titleColor The title color attribute value to use.
     */
    fun setTitleColor(titleColor: Int) {
        mTitleColor = titleColor
        invalidateTextPaints()
    }

    companion object {
        private const val DEFAULT_TITLE_COLOR = Color.WHITE
        private val DEFAULT_BACKGROUND_COLOR = Color.rgb(42, 94, 165)
        private const val DEFAULT_VIEW_SIZE = 96
        private const val DEFAULT_TITLE_SIZE = 25f
        private const val DEFAULT_TITLE = "A"
    }
}