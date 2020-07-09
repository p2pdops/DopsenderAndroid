package p2pdops.dopsender.pagers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.layout_screen.view.*
import p2pdops.dopsender.R

data class ScreenItem(val Title: String, val Description: String, val ScreenImg: Int)

class IntroViewPagerAdapter(
    private val mContext: Context,
    private val mListScreen: List<ScreenItem>
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutScreen: View = inflater.inflate(R.layout.layout_screen, container, false)
        layoutScreen.intro_title.text = mListScreen[position].Title
        layoutScreen.intro_description.text = mListScreen[position].Description
        layoutScreen.intro_img.setImageResource(mListScreen[position].ScreenImg)
        container.addView(layoutScreen)
        return layoutScreen
    }

    override fun getCount(): Int {
        return mListScreen.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }
}