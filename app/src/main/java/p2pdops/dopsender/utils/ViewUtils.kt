package p2pdops.dopsender.utils

import android.view.View
import android.view.animation.AnimationUtils
import p2pdops.dopsender.R

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.bulge() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(
            context, R.anim.bulge
        )
        startAnimation(animation)
    }
}

fun View.shrink() {
    this.visibility = View.GONE
    val animation = AnimationUtils.loadAnimation(
        context, R.anim.shrink
    )
    startAnimation(animation)
}