package p2pdops.dopsender.utils

import androidx.viewpager.widget.ViewPager.OnPageChangeListener

class Abs {
    var listener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {}
        override fun onPageScrollStateChanged(state: Int) {}
    }
}