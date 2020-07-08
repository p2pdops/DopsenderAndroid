package p2pdops.dopsender

import android.app.Activity
import android.content.Intent
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

import kotlinx.android.synthetic.main.activity_docs_selector.*

import p2pdops.dopsender.modals.FolderInfo

import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.show
import java.io.File

import p2pdops.dopsender.pagers.DocsSelectorPagerAdapter
import p2pdops.dopsender.utils.Constants

import p2pdops.dopsender.utils.humanizeBytes


class DocsSelectorActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "VideosSelectorActivity"
    }

    private var selectedDocs: ArrayList<File> = ArrayList()

    private var menu: Menu? = null

    private var totalSize = 0L

    private var currentPosition = 0


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docs_selector)

        send_fab.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        FetchDocFoldersAsyncTask(this).execute()

        supportActionBar?.title = "Your documents"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class FetchDocFoldersAsyncTask(private val activity: DocsSelectorActivity) :
        AsyncTask<Void, Void, ArrayList<FolderInfo>>() {

        override fun doInBackground(vararg params: Void?): ArrayList<FolderInfo> {
            val foldersList = ArrayList<FolderInfo>()

            Log.d("ImageUtils: ", "GetAllImagesFoldersCursor")

            val col = MediaStore.Images.Media.DATA

            val bucketDisplayName = MediaStore.Files.FileColumns.PARENT


            val columns = arrayOf(
                "DISTINCT " + MediaStore.Files.FileColumns.PARENT,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_MODIFIED
            )

            val extensions = arrayOf("%pdf", "%csv", "%doc", "%docx", "%xls", "%xlsx")

            val lSelectionString = "GROUP BY ($bucketDisplayName"

            val lCursor = activity.contentResolver!!.query(
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
            activity.handleDocsAvailable(result)
            Log.d(TAG, "onPostExecute: $result")
        }
    }


    private fun handleDocsAvailable(folders: ArrayList<FolderInfo>) {
        setBtmBarText("Select documents to continue")

        view_pager.show()
        no_docs.hide()

        val sectionsPagerAdapter = DocsSelectorPagerAdapter(
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
                this@DocsSelectorActivity.currentPosition = position
                Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                invalidateOptionsMenu()
                bottom_app_bar.performShow()
            }
        }

        view_pager.addOnPageChangeListener(listener)


        send_fab.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.FILES, selectedDocs)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private val allSelectedPositions = ArrayList<Int>()

    fun selectDocAll(files: List<File>, position: Int) {
        Log.d(TAG, "selectVideosAll: ")

        deSelectDocAll(files, position).apply {
            selectedDocs.addAll(files)
        }.apply {
            totalSize = selectedDocs.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            updateStatus()
            if (!allSelectedPositions.contains(position)) allSelectedPositions.add(position)
        }
    }

    fun deSelectDocAll(files: List<File>, position: Int) {
        Log.d(TAG, "deSelectVideosAll: ")
        selectedDocs.removeAll(files).apply {
            totalSize = selectedDocs.fold(0L) { sum, element -> sum + element.length() }
        }.apply {
            if (allSelectedPositions.contains(position)) allSelectedPositions.remove(position)
            updateStatus()
        }
    }

    fun selectDoc(file: File) {
        if (!selectedDocs.contains(file)) {
            selectedDocs.add(file)
            totalSize += file.length()
            updateStatus()
        }
    }

    fun deSelectDoc(file: File) {
        if (selectedDocs.contains(file)) {
            selectedDocs.remove(file)
            totalSize -= file.length()
            if (totalSize < 0) totalSize = 0
            updateStatus()
        }
    }


    private fun updateStatus() {
        Log.d(TAG, "updateStatus: $totalSize $selectedDocs")
        if (selectedDocs.isEmpty() || totalSize == 0L) {
            send_fab.hide()
            setBtmBarText("Select documents to continue")
            return
        }
        if (send_fab.isOrWillBeHidden) {
            send_fab.show()
            bottom_app_bar.performShow()
        }
        setBtmBarText("${humanizeBytes(totalSize)} | ${selectedDocs.size} ${if (selectedDocs.size == 1) "document" else "documents"} selected")
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
