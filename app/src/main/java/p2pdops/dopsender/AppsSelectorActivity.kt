package p2pdops.dopsender

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.android.synthetic.main.activity_apps_selector.*
import p2pdops.dopsender.R.layout.activity_apps_selector
import p2pdops.dopsender.modals.AppData
import p2pdops.dopsender.pagers.AppsSelectorPagerAdapter
import p2pdops.dopsender.utils.Constants
import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.utils.show


class AppsSelectorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AppsSelectorActivity"
    }

    private var menu: Menu? = null

    private var totalSize = 0L

    private var currentPosition = 0

    private val allSelectedPositions = ArrayList<Int>()

    private val selectedApps = ArrayList<AppData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_apps_selector)
        send_fab.hide()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        supportActionBar?.title = "Your apps"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view_pager.show()
        no_apps.hide()

        setBtmBarText("Select apps to continue")

        val sectionsPagerAdapter = AppsSelectorPagerAdapter(
            supportFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )

        view_pager.adapter = sectionsPagerAdapter

        tabs.setupWithViewPager(view_pager)

        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                this@AppsSelectorActivity.currentPosition = position
                Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                invalidateOptionsMenu()
                bottom_app_bar.performShow()
            }
        }

        view_pager.addOnPageChangeListener(listener)


        send_fab.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.APPS, selectedApps)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }


    }

    fun selectAllApps(apps: List<AppData>, position: Int) {
        Log.d(TAG, "selectAllApps: ")
        deSelectAllApps(apps, position).apply {
            selectedApps.addAll(apps)
        }.apply {
            totalSize = selectedApps.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            updateStatus()
            if (!allSelectedPositions.contains(position)) allSelectedPositions.add(position)
        }
    }

    fun deSelectAllApps(apps: List<AppData>, position: Int) {
        Log.d(TAG, "deSelectAllApps: apps total size: $totalSize")
        selectedApps.removeAll(apps).apply {
            totalSize = selectedApps.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (allSelectedPositions.contains(position)) allSelectedPositions.remove(position)
            updateStatus()
        }

    }

    fun selectApp(appData: AppData) {
        if (!selectedApps.contains(appData)) {
            selectedApps.add(appData)
            totalSize += appData.length()
            updateStatus()
        }
    }

    fun deSelectApp(appData: AppData) {
        if (selectedApps.contains(appData)) {
            selectedApps.remove(appData)
            totalSize -= appData.length()
            if (totalSize < 0) totalSize = 0
            updateStatus()
        }
    }

    private fun updateStatus() {
        Log.d(TAG, "updateStatus: total apps size: $totalSize, apps: ${selectedApps.size}")
        if (selectedApps.isEmpty() || totalSize == 0L) {
            send_fab.hide()
            setBtmBarText("Select apps to continue")
            return
        }

        if (send_fab.isOrWillBeHidden) {
            send_fab.show()
            bottom_app_bar.performShow()
        }

        setBtmBarText("${humanizeBytes(totalSize)} | ${selectedApps.size} ${if (selectedApps.size == 1) "app" else "apps"} selected")
    }

    private fun setBtmBarText(text: String) {
        Log.d(TAG, "setBtmBarText: $text")
        bottom_app_bar_title.text = text
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: ")
        this.menu = menu
        menuInflater.inflate(R.menu.select_menu, menu)
        allSelectedPositions.contains(currentPosition).apply {
            if (this) showDeSelectAll() else showSelectAll()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ")
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onPrepareOptionsMenu: ")
        return super.onPrepareOptionsMenu(menu)
    }

    fun showSelectAll() {
        Log.d(TAG, "showSelectAll: ")
        menu?.findItem(R.id.select_all_grp)?.isVisible = true
        menu?.findItem(R.id.deselect_all_grp)?.isVisible = false
    }

    fun showDeSelectAll() {
        Log.d(TAG, "showDeSelectAll: called")
        menu?.findItem(R.id.select_all_grp)?.isVisible = false
        menu?.findItem(R.id.deselect_all_grp)?.isVisible = true
    }
}