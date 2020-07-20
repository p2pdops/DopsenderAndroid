package p2pdops.dopsender.selectors.ui.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.io.File

class MediaSelectorPagerAdapter(
    private val fileType: String,
    private val folders: Array<File>,
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = MediaSelectorFragment.newInstance(
        position, folders[position], fileType
    )

    override fun getPageTitle(position: Int): CharSequence? = folders[position].name

    override fun getCount(): Int = folders.size

}