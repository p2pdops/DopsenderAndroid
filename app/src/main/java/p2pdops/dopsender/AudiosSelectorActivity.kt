package p2pdops.dopsender

import android.app.Activity
import android.content.Intent
import android.os.*
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView

import p2pdops.dopsender.modals.FolderInfo

import java.io.File

import kotlinx.android.synthetic.main.activity_audios_selector.*
import kotlinx.android.synthetic.main.activity_audios_selector.bottom_app_bar
import kotlinx.android.synthetic.main.activity_audios_selector.bottom_app_bar_title
import kotlinx.android.synthetic.main.activity_audios_selector.send_fab
import kotlinx.android.synthetic.main.activity_audios_selector.tabs
import kotlinx.android.synthetic.main.activity_audios_selector.view_pager

import p2pdops.dopsender.pagers.AudiosSelectorPagerAdapter
import p2pdops.dopsender.utils.*

class AudiosSelectorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideosSelectorActivity"
    }

    private var selectedAudios: ArrayList<File> = ArrayList()

    private var menu: Menu? = null

    private var totalSize = 0L

    private var currentPosition = 0

    private val allSelectedPositions = ArrayList<Int>()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audios_selector)

        if (!getSendFabHelperShown()) {

            TapTargetView.showFor(this,
                TapTarget.forView(
                    send_fab,
                    "To send...",
                    "Click on this to send selected."
                )
                    .outerCircleColor(R.color.pureWhite) // Specify a color for the outer circle
                    .outerCircleAlpha(0.98f) // Specify the alpha amount for the outer circle
                    .targetCircleColor(R.color.unPureWhite) // Specify a color for the target circle
                    .titleTextSize(20) // Specify the size (in sp) of the title text
                    .titleTextColor(R.color.pureBlack) // Specify the color of the title text
                    .descriptionTextSize(16) // Specify the size (in sp) of the description text
                    .descriptionTextColor(R.color.pureBlack) // Specify the color of the description text
                    .textColor(R.color.black80) // Specify a color for both the title and description text
                    .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                    .dimColor(R.color.unPureWhite) // If set, will dim behind the view with 30% opacity of the given color
                    .drawShadow(true) // Whether to draw a drop shadow or not
                    .cancelable(true) // Whether tapping outside the outer circle dismisses the view
                    .tintTarget(true) // Whether to tint the target view's color
                    .transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                    .targetRadius(15),  // Specify the target radius (in dp)
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        Toast.makeText(
                            this@AudiosSelectorActivity,
                            "Ready to go!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            setSendFabHelperShown()
        } else send_fab.hide()

        FetchAudioFoldersAsyncTask(this).execute()

        supportActionBar?.title = "Your Audios"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class FetchAudioFoldersAsyncTask(private val activity: AudiosSelectorActivity) :
        AsyncTask<Void, Void, ArrayList<FolderInfo>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<FolderInfo> {
            val foldersList = ArrayList<FolderInfo>()
            val lCursor: Cursor
            Log.d("FetchAudioFolders", "called")

            val lUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI


            val bucketDisplayName = MediaStore.Audio.AudioColumns.ALBUM
            val lProjection = arrayOf(
                "DISTINCT $bucketDisplayName",
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED
            )

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
            activity.handleAudiosAvailable(result)

        }

    }

    private fun handleAudiosAvailable(folders: ArrayList<FolderInfo>) {
        setBtmBarText("Select audios to continue")

        view_pager.show()
        no_audios.hide()

        val sectionsPagerAdapter = AudiosSelectorPagerAdapter(
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
                this@AudiosSelectorActivity.currentPosition = position
                Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                invalidateOptionsMenu()
                bottom_app_bar.performShow()
            }
        }

        view_pager.addOnPageChangeListener(listener)


        send_fab.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.FILES, selectedAudios)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    fun selectAudioAll(files: List<File>, position: Int) {
        Log.d(TAG, "selectAudioAll: ")

        deSelectAudioAll(files, position).apply {
            selectedAudios.addAll(files)
        }.apply {
            totalSize = selectedAudios.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            updateStatus()
            if (!allSelectedPositions.contains(position)) allSelectedPositions.add(position)
        }
    }

    fun deSelectAudioAll(files: List<File>, position: Int) {
        Log.d(TAG, "deSelectAudioAll: ")
        selectedAudios.removeAll(files).apply {
            totalSize = selectedAudios.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (allSelectedPositions.contains(position)) allSelectedPositions.remove(position)
            updateStatus()
        }
    }

    fun selectAudio(file: File) {
        if (!selectedAudios.contains(file)) {
            selectedAudios.add(file)
            totalSize += file.length()
            updateStatus()
        }
    }

    fun deSelectAudio(file: File) {
        if (selectedAudios.contains(file)) {
            selectedAudios.remove(file)
            totalSize -= file.length()
            if (totalSize < 0) totalSize = 0
            updateStatus()
        }
    }

    private fun updateStatus() {
        Log.d(TAG, "updateStatus: $totalSize $selectedAudios")
        if (selectedAudios.isEmpty() || totalSize == 0L) {
            send_fab.hide()
            setBtmBarText("Select audios to continue")
            return
        }
        if (send_fab.isOrWillBeHidden) {
            send_fab.show()
            bottom_app_bar.performShow()
        }
        setBtmBarText("${humanizeBytes(totalSize)} | ${selectedAudios.size} ${if (selectedAudios.size == 1) "audio" else "audios"} selected")
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
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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