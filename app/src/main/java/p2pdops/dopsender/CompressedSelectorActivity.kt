package p2pdops.dopsender

import android.app.Activity
import android.content.Intent
import android.os.*
import android.database.Cursor
import android.graphics.Typeface

import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import kotlinx.android.synthetic.main.activity_compressed_selector.*
import kotlinx.android.synthetic.main.activity_compressed_selector.bottom_app_bar
import kotlinx.android.synthetic.main.activity_compressed_selector.bottom_app_bar_title
import kotlinx.android.synthetic.main.activity_compressed_selector.send_fab
import kotlinx.android.synthetic.main.activity_compressed_selector.tabs
import kotlinx.android.synthetic.main.activity_compressed_selector.view_pager

import p2pdops.dopsender.modals.FolderInfo

import java.io.File

import p2pdops.dopsender.pagers.CompressedSelectorPagerAdapter
import p2pdops.dopsender.utils.*


class CompressedSelectorActivity : AppCompatActivity(), Handler.Callback {


    companion object {
        private const val TAG = "CompressedSelActivity"
    }

    private var selectedCompressed: ArrayList<File> = ArrayList()

    private var menu: Menu? = null

    private var showingFab = false

    private var totalSize = 0L

    private var currentPosition = 0


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compressed_selector)


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
                            this@CompressedSelectorActivity,
                            "Ready to go!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            setSendFabHelperShown()
        } else send_fab.hide()

        FetchCompressedFoldersAsyncTask(this).execute()

        supportActionBar?.title = "Your archives"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class FetchCompressedFoldersAsyncTask(private val activity: CompressedSelectorActivity) :
        AsyncTask<Void, Void, ArrayList<FolderInfo>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<FolderInfo> {
            val foldersList = ArrayList<FolderInfo>()
            val lCursor: Cursor
            Log.d("ImageUtils: ", "GetAllImagesFoldersCursor")

//            val lUri: Uri =
            val lProjection = arrayOf(
                "DISTINCT bucket_display_name",
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED
            )

            val col = MediaStore.Images.Media.DATA

            val bucketDisplayName = MediaStore.Files.FileColumns.PARENT


            val columns = arrayOf(
                "DISTINCT " + MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED
            )

            val extensions = arrayOf("%zip", "%rar", "%obb")

            val lSelectionString = "GROUP BY ($bucketDisplayName"

            lCursor = activity.contentResolver!!.query(
                MediaStore.Files.getContentUri("external"),
                columns,
                "$col LIKE ? OR $col LIKE ? OR $col LIKE ? OR $col LIKE ? OR $col LIKE ? OR $col LIKE ? ) $lSelectionString",
                extensions,
                null
            )!!


            Log.d(TAG, "doInBackground: ${lCursor.extras}")
            val dataIndex: Int = lCursor.getColumnIndexOrThrow("_data")
            val dateIndex: Int =
                lCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)


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
            activity.handleCompressedAvailable(result)
            Log.d(TAG, "onPostExecute: $result")
        }
    }


    private fun handleCompressedAvailable(folders: ArrayList<FolderInfo>) {
        setBtmBarText("Select archives to continue")

        view_pager.show()
        no_docs.hide()

        val sectionsPagerAdapter = CompressedSelectorPagerAdapter(
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
                this@CompressedSelectorActivity.currentPosition = position
                Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                invalidateOptionsMenu()
                bottom_app_bar.performShow()
            }
        }

        view_pager.addOnPageChangeListener(listener)


        send_fab.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.FILES, selectedCompressed)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }


    private fun updateStatus() {
        Log.d(TAG, "updateStatus: $totalSize $selectedCompressed")
        if (selectedCompressed.isEmpty() || totalSize == 0L) {
            send_fab.hide()
            setBtmBarText("Select archives to continue")
            return
        }
        if (send_fab.isOrWillBeHidden) {
            send_fab.show()
            bottom_app_bar.performShow()
        }
        setBtmBarText("${humanizeBytes(totalSize)} | ${selectedCompressed.size} ${if (selectedCompressed.size == 1) "archive" else "archives"} selected")
    }

    private fun setBtmBarText(text: String) {
        Log.d(TAG, "setBtmBarText: $text")
        bottom_app_bar_title.text = text
    }


    override
    fun handleMessage(msg: Message): Boolean {
        return true
    }

    fun selectCompressed(file: File) {
        if (!selectedCompressed.contains(file)) {
            selectedCompressed.add(file)
            totalSize += file.length()
            updateStatus()
        }
    }

    fun selectCompressedAll(files: List<File>, position: Int) {
        Log.d(TAG, "selectVideosAll: ")

        deSelectCompressedAll(files, position).apply {
            selectedCompressed.addAll(files)
        }.apply {
            totalSize = selectedCompressed.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (!allSelectedPositions.contains(position)) allSelectedPositions.add(position)
            updateStatus()
        }
    }


    private val allSelectedPositions = ArrayList<Int>()

    fun deSelectCompressedAll(files: List<File>, position: Int) {
        Log.d(TAG, "deSelectVideosAll: ")
        selectedCompressed.removeAll(files).apply {
            totalSize = selectedCompressed.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (allSelectedPositions.contains(position)) allSelectedPositions.remove(position)
            updateStatus()
        }
    }

    fun deSelectCompressed(file: File) {
        if (selectedCompressed.contains(file)) {
            selectedCompressed.remove(file)
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
