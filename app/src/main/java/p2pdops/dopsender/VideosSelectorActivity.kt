package p2pdops.dopsender

import android.app.Activity
import android.content.Intent
import android.os.*
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_apps_selector.*


import p2pdops.dopsender.modals.FolderInfo

import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.show
import java.io.File
import kotlin.math.max

import kotlinx.android.synthetic.main.activity_videos_selector.*
import kotlinx.android.synthetic.main.activity_videos_selector.bottom_app_bar
import kotlinx.android.synthetic.main.activity_videos_selector.bottom_app_bar_title
import kotlinx.android.synthetic.main.activity_videos_selector.send_fab
import kotlinx.android.synthetic.main.activity_videos_selector.tabs
import kotlinx.android.synthetic.main.activity_videos_selector.view_pager
import p2pdops.dopsender.pagers.VideosSelectorPagerAdapter
import p2pdops.dopsender.utils.Constants.Companion.FILES
import p2pdops.dopsender.utils.humanizeBytes


class VideosSelectorActivity : AppCompatActivity(), Handler.Callback {


    companion object {
        private const val TAG = "VideosSelectorActivity"
    }

    private var selectedVideos: ArrayList<File> = ArrayList()

    private var menu: Menu? = null

    private val handler = Handler(this)

    private val allSelectedPositions = ArrayList<Int>()

    private var totalSize = 0L

    private var currentPosition = 0


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos_selector)

        send_fab.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        FetchVideoFoldersAsyncTask(this).execute()

        supportActionBar?.title = "Your videos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun handleVideosAvailable(folders: ArrayList<FolderInfo>) {
        setBtmBarText("Select videos to continue")

        view_pager.show()
        no_videos.hide()

        val sectionsPagerAdapter = VideosSelectorPagerAdapter(
            folders,
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
                this@VideosSelectorActivity.currentPosition = position
                invalidateOptionsMenu()
                Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                bottom_app_bar.performShow()
            }
        }

        view_pager.addOnPageChangeListener(listener)


        send_fab.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(FILES, selectedVideos)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    class FetchVideoFoldersAsyncTask(private val activity: VideosSelectorActivity) :
        AsyncTask<Void, Void, ArrayList<FolderInfo>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<FolderInfo> {
            val foldersList = ArrayList<FolderInfo>()
            val lCursor: Cursor
            Log.d("FetchVideoFolders", "GetAllVideosFoldersCursor")

            val lUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val lProjection = arrayOf(
                "DISTINCT bucket_display_name",
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED
            )

            val bucketDisplayName = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME

            val lSelectionString = "$bucketDisplayName IS NOT NULL) GROUP BY ($bucketDisplayName"
            lCursor = activity.contentResolver.query(
                lUri, lProjection, lSelectionString,
                null, "date_modified DESC"
            )!!

            val dataIndex: Int = lCursor.getColumnIndexOrThrow("_data")
            val dateIndex: Int =
                lCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)


            var folderName: String
            var folderPath: String
            var dateTaken: Long

            while (lCursor.moveToNext()) {


                folderPath = lCursor.getString(dataIndex)
                folderPath = folderPath.substring(0, folderPath.lastIndexOf("/"))
                dateTaken = lCursor.getLong(dateIndex)

                val file = File(folderPath)
                folderName = file.name

                foldersList.add(FolderInfo(folderName, folderPath, dateTaken))
                Log.d("FetchFolders", "folders : $folderName $folderPath")

            }

            lCursor.close()

            return foldersList
        }

        override fun onPostExecute(result: ArrayList<FolderInfo>) {
            super.onPostExecute(result)
            activity.handleVideosAvailable(result)

        }

    }

    private fun updateStatus() {
        Log.d(TAG, "updateStatus: $totalSize $selectedVideos")
        if (selectedVideos.isEmpty() || totalSize == 0L) {
            send_fab.hide()
            setBtmBarText("Select videos to continue")
            return
        }
        if (send_fab.isOrWillBeHidden) send_fab.show()
        setBtmBarText("${humanizeBytes(totalSize)} | ${selectedVideos.size} ${if (selectedVideos.size == 1) "video" else "videos"} selected")
    }

    private fun setBtmBarText(text: String) {
        Log.d(TAG, "setBtmBarText: $text")
        bottom_app_bar_title.text = text
    }


    override
    fun handleMessage(msg: Message): Boolean {
        return true
    }

    fun selectVideo(file: File) {
        if (!selectedVideos.contains(file)) {
            selectedVideos.add(file)
            totalSize += file.length()
            updateStatus()
        }
    }

    fun selectVideosAll(files: List<File>, position: Int) {
        Log.d(TAG, "selectVideosAll: ")

        deSelectVideosAll(files, position).apply {
            selectedVideos.addAll(files)
        }.apply {
            totalSize = selectedVideos.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (!allSelectedPositions.contains(position)) allSelectedPositions.add(position)
            updateStatus()
        }
    }

    fun deSelectVideosAll(files: List<File>, position: Int) {
        Log.d(TAG, "deSelectVideosAll: ")
        selectedVideos.removeAll(files).apply {
            totalSize = selectedVideos.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (allSelectedPositions.contains(position)) allSelectedPositions.remove(position)
            updateStatus()
        }
    }

    fun deSelectVideo(file: File) {
        if (selectedVideos.contains(file)) {
            selectedVideos.remove(file)
            totalSize -= file.length()
            if (totalSize < 0) totalSize = 0
            updateStatus()
        }
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