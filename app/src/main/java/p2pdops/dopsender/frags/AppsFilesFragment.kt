package p2pdops.dopsender.frags

import android.app.Activity
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.frag_apps.view.*

import p2pdops.dopsender.AppsSelectorActivity

import p2pdops.dopsender.R

import p2pdops.dopsender.modals.AppData

import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.viewmodels.AppsViewModel

import java.util.*

import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.item_app.view.*
import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.show

class AppsFilesFragment : Fragment() {

    class AppFilesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val TAG = "AppsFilesFragment"

        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"

        @JvmStatic
        fun newInstance(sectionNumber: Int): AppsFilesFragment {
            return AppsFilesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


    private var mActivity: AppsSelectorActivity? = null
    private lateinit var viewModel: AppsViewModel
    private var audiosRecyclerView: RecyclerView? = null
    private var appFilesAdapter: RecyclerView.Adapter<AppFilesViewHolder>? = null
    var apksList: List<AppData> = ArrayList()
    var selectedApksList: ArrayList<AppData> = ArrayList()

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mActivity = activity as AppsSelectorActivity
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "$activity must implement onSomeEventListener"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val rootView = inflater.inflate(R.layout.frag_apps, container, false)
        viewModel = ViewModelProvider(this).get(AppsViewModel::class.java)

        viewModel.selectedApps.observe(this as LifecycleOwner, Observer { t: ArrayList<AppData> ->
            if (t.containsAll(apksList)) mActivity?.showDeSelectAll()
            else mActivity?.showSelectAll()
            selectedApksList = t
            Log.d(TAG, "change:: $selectedApksList")
            appFilesAdapter?.notifyDataSetChanged()
        })

        audiosRecyclerView = rootView.appsRecycler

        FetchApkFiles(this).execute()

        appFilesAdapter =
            object : RecyclerView.Adapter<AppFilesViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): AppFilesViewHolder {
                    return AppFilesViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
                    )
                }

                override fun getItemCount(): Int = apksList.size

                override fun onBindViewHolder(holder: AppFilesViewHolder, position: Int) {

                    val apk = apksList[position]

                    if (selectedApksList.contains(apksList[position])) {
                        holder.itemView.checked.show()
                    } else {
                        holder.itemView.checked.hide()
                    }

                    holder.itemView.appName.text = apk.appName
                    holder.itemView.appName.isSelected = true
                    holder.itemView.appSize.text = humanizeBytes(apk.length(), 0)

                    Glide.with(holder.itemView).load(apk.iconDrawable!!).into(holder.itemView.appIcon)




                    holder.itemView.setOnClickListener {
                        if (!selectedApksList.contains(apk)) {
                            viewModel.addSelectedApps(apk)
                            mActivity!!.selectApp(apk)
                            holder.itemView.checked.show()
                        } else {
                            viewModel.removeSelectedApps(apk)
                            mActivity!!.selectApp(apk)
                            holder.itemView.checked.hide()
                        }
                    }
                }

            }

        audiosRecyclerView!!.setHasFixedSize(true)

        audiosRecyclerView!!.layoutManager = GridLayoutManager(context, 2)

        audiosRecyclerView?.adapter = appFilesAdapter

        return rootView
    }

    class FetchApkFiles(
        private val fragment: AppsFilesFragment
    ) : AsyncTask<Void, Void, List<AppData>>() {
        override fun doInBackground(vararg params: Void?): List<AppData> {

            val files = ArrayList<AppData>()

            val col = MediaStore.Images.Media.DATA

            val columns = arrayOf(MediaStore.Files.FileColumns.DATA)

            val lCursor = fragment.requireContext().contentResolver!!.query(
                MediaStore.Files.getContentUri("external"),
                columns,
                "$col LIKE ?",
                arrayOf("%apk"),
                "date_modified DESC"
            )!!

            val dataIndex: Int = lCursor.getColumnIndexOrThrow("_data")

            while (lCursor.moveToNext()) {
                val filePath = lCursor.getString(dataIndex)
                val pm = fragment.context?.packageManager
                pm?.getPackageArchiveInfo(
                    filePath,
                    PackageManager.GET_META_DATA
                )?.let {

                    it.applicationInfo.sourceDir = filePath
                    it.applicationInfo.publicSourceDir = filePath

                    val appName = it.applicationInfo.loadLabel(pm).toString()

                    val appData = AppData(
                        appName = appName[0].toUpperCase() + appName.substring(1) + ".apk"
                            .toLowerCase(Locale.getDefault()),
                        appPackageName = it.packageName,
                        appFilePath = filePath,
                        iconDrawable = it.applicationInfo.loadIcon(pm)
                    )
                    files.add(appData)
                }
            }

            lCursor.close()

            return files
        }

        override
        fun onPostExecute(result: List<AppData>) {
            super.onPostExecute(result)
            fragment.apksList = result
            fragment.notifyAudiosAvailable()
        }
    }

    private fun notifyAudiosAvailable() {
        Log.d(TAG, "notifyAudiosAvailable: ${apksList.size}")
        appFilesAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {
            R.id.select_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: select")
                mActivity!!.selectAllApps(apksList, position).also {
                    viewModel.selectedAllApps(apksList)
                    mActivity!!.showDeSelectAll()
                }


                true
            }
            R.id.deselect_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: deselect")
                mActivity!!.deSelectAllApps(apksList, position).also {
                    mActivity!!.showSelectAll()
                }.also {
                    viewModel.deSelectAllApps(apksList)
                }
                true
            }

            else -> {
                false
            }
        }
    }
}