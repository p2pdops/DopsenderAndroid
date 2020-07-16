package p2pdops.dopsender.pagers


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import p2pdops.dopsender.frags.AppsFilesFragment
import p2pdops.dopsender.frags.AppsInstalledFragment



class AppsSelectorPagerAdapter(
    fm: FragmentManager,
    behavior: Int
) : FragmentPagerAdapter(fm, behavior) {

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> AppsInstalledFragment.newInstance(position)
        else -> AppsFilesFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "Installed"
        else -> "Apk files"
    }

    override fun getCount(): Int = 2

}