package p2pdops.dopsender.pagers


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import p2pdops.dopsender.modals.FolderInfo
import p2pdops.dopsender.frags.DocsFolderFragment


class DocsSelectorPagerAdapter(
    private val foldersList: ArrayList<FolderInfo>,
    fm: FragmentManager,
    behavior: Int
) : FragmentPagerAdapter(fm, behavior) {

    override fun getItem(position: Int): Fragment =
        DocsFolderFragment.newInstance(position, folderPath = foldersList[position].path)

    override fun getPageTitle(position: Int): CharSequence? = foldersList[position].name


    override fun getCount(): Int = foldersList.size

}