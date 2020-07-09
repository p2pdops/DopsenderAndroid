package p2pdops.dopsender.utils

import android.graphics.Rect
import android.text.TextPaint
import kotlin.math.ceil


/**
 * Pad a target string of text with spaces on the right to fill a target
 * width
 *
 * @param text The target text
 * @param paint The TextPaint used to measure the target text and
 * whitespaces
 * @param width The target width to fill
 * @return the original text with extra padding to fill the width
 */
fun padText(text: CharSequence, paint: TextPaint, width: Int): CharSequence? {

    // First measure the width of the text itself
    val textbounds = Rect()
    paint.getTextBounds(text.toString(), 0, text.length, textbounds)
    /**
     * check to see if it does indeed need padding to reach the target width
     */
    if (textbounds.width() > width) {
        return text
    }

    /*
     * Measure the text of the space character (there's a bug with the
     * 'getTextBounds() method of Paint that trims the white space, thus
     * making it impossible to measure the width of a space without
     * surrounding it in arbitrary characters)
     */
    val workaroundString = "a a"
    val spacebounds = Rect()
    paint.getTextBounds(workaroundString, 0, workaroundString.length, spacebounds)
    val abounds = Rect()
    paint.getTextBounds(
        charArrayOf(
            'a'
        ), 0, 1, abounds
    )
    val spaceWidth: Int = spacebounds.width() - abounds.width() * 2

    /*
     * measure the amount of spaces needed based on the target width to fill
     * (using Math.ceil to ensure the maximum whole number of spaces)
     */
    val amountOfSpacesNeeded =
        ceil(((width - textbounds.width()) / spaceWidth).toDouble()).toInt()

    // pad with spaces til the width is less than the text width
    return if (amountOfSpacesNeeded > 0) padRight(
        text.toString(), text.toString().length
                + amountOfSpacesNeeded
    ) else text
}

/**
 * Pads a string with white space on the right of the original string
 *
 * @param s The target string
 * @param n The new target length of the string
 * @return The target string padded with whitespace on the right to its new
 * length
 */
fun padRight(s: String?, n: Int): String? {
    return String.format("%1$-" + n + "s", s)
}