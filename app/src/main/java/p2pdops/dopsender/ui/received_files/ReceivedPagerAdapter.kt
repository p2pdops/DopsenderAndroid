package p2pdops.dopsender.ui.received_files

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.utils.fileTypes


class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return ReceivedFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? = fileTypes[position].name

    override fun getCount(): Int = fileTypes.size
}