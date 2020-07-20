package p2pdops.dopsender.selectors.ui.apps

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
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
import kotlinx.android.synthetic.main.item_app.view.*
import p2pdops.dopsender.selectors.AppsSelectorActivity

import p2pdops.dopsender.R
import p2pdops.dopsender.modals.AppData


import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.utils.show
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class AppsInstalledFragment : Fragment() {

    class AppsInstalledViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val TAG = "AppsInstalledFragment"

        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"

        @JvmStatic
        fun newInstance(sectionNumber: Int): AppsInstalledFragment {
            return AppsInstalledFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


    private var mActivity: AppsSelectorActivity? = null
    private lateinit var viewModel: AppsViewModel
    private var appsInstalledRecyclerView: RecyclerView? = null
    private var appsInstalledAdapter: RecyclerView.Adapter<AppsInstalledViewHolder>? = null
    var appsInstalledList: List<AppData> = ArrayList()
    var selectedAppsList: ArrayList<AppData> = ArrayList()

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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val rootView = inflater.inflate(R.layout.frag_apps, container, false)
        viewModel = ViewModelProvider(this).get(AppsViewModel::class.java)

        viewModel.selectedApps.observe(this as LifecycleOwner, Observer { t: ArrayList<AppData> ->
            selectedAppsList = t
            if (t.containsAll(appsInstalledList)) mActivity?.showDeSelectAll()
            else mActivity?.showSelectAll()
            appsInstalledAdapter?.notifyDataSetChanged()
            Log.d(TAG, "change:: $selectedAppsList")
        })

        appsInstalledRecyclerView = rootView.appsRecycler


        FetchInstalledApps(
            this
        ).execute()

        appsInstalledAdapter =
            object : RecyclerView.Adapter<AppsInstalledViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): AppsInstalledViewHolder {
                    return AppsInstalledViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
                    )
                }

                override fun getItemCount(): Int = appsInstalledList.size

                override fun onBindViewHolder(holder: AppsInstalledViewHolder, position: Int) {

                    val app = appsInstalledList[position]

                    if (selectedAppsList.contains(appsInstalledList[position])) {
                        holder.itemView.appChecked.show()
                    } else {
                        holder.itemView.appChecked.hide()
                    }

                    holder.itemView.appName.text = app.appName
                    holder.itemView.appName.isSelected = true
                    holder.itemView.appSize.text = humanizeBytes(app.length(), 0)

                    val pm = context?.packageManager
                    Glide.with(requireContext())
                        .load(pm?.getApplicationIcon(app.appPackageName))
                        .error(R.drawable.ic_apk)
                        .thumbnail(0.2f)
                        .centerCrop()
                        .into(holder.itemView.appIcon)


                    holder.itemView.setOnClickListener {
                        if (!selectedAppsList.contains(appsInstalledList[position])) {
                            viewModel.addSelectedApps(
                                appsInstalledList[position]
                            )
                            mActivity!!.selectApp(appsInstalledList[position])
                            holder.itemView.appChecked.show()
                        } else {
                            viewModel.removeSelectedApps(appsInstalledList[position])
                            mActivity!!.deSelectApp(appsInstalledList[position])
                            holder.itemView.appChecked.hide()
                        }
                    }
                }

            }

        appsInstalledRecyclerView!!.setHasFixedSize(true)

        appsInstalledRecyclerView!!.layoutManager = GridLayoutManager(context, 2)

        appsInstalledRecyclerView?.adapter = appsInstalledAdapter

        return rootView
    }

    class FetchInstalledApps(private val fragment: AppsInstalledFragment) :
        AsyncTask<Void, Void, List<AppData>>() {
        override fun doInBackground(vararg params: Void?): List<AppData> {

            val pm: PackageManager = fragment.requireContext().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val appsArray = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            val appsList = ArrayList<AppData>()
            var appName: String
            for (info in appsArray) {
                appName = info.loadLabel(pm).toString()
                val appData = AppData(
                    appName = appName[0].toUpperCase() + appName.substring(1)
                        .toLowerCase(Locale.getDefault()),
                    appPackageName = info.activityInfo.packageName,
                    appFilePath = info.activityInfo.applicationInfo.publicSourceDir
                )
                if (!appsList.contains(appData))
                    appsList.add(appData)
            }

            appsList.sortWith(Comparator { d1, d2 -> d1.appName.compareTo(d2.appName) })

            return appsList

        }

        override
        fun onPostExecute(result: List<AppData>) {
            super.onPostExecute(result)
            fragment.appsInstalledList = result
            fragment.notifyAppsAvailable()
        }
    }

    private fun notifyAppsAvailable() {
        Log.d(TAG, "notifyAudiosAvailable: ${appsInstalledList.size}")
        appsInstalledAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {
            R.id.select_all_grp -> {

                Log.d(TAG, "onOptionsItemSelected: select")
                mActivity!!.selectAllApps(appsInstalledList, position).also {
                    viewModel.selectedAllApps(appsInstalledList)
                    mActivity!!.showDeSelectAll()
                }

                true
            }
            R.id.deselect_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: deselect")
                mActivity!!.deSelectAllApps(appsInstalledList, position).also {
                    mActivity!!.showSelectAll()
                }.also {
                    viewModel.deSelectAllApps(appsInstalledList)
                }
                true
            }

            else -> {
                false
            }
        }
    }
}