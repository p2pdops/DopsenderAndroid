package p2pdops.dopsender.selectors

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_media_selectors.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.selectors.ui.media.MediaSelectorPagerAdapter
import p2pdops.dopsender.selectors.ui.media.MediaSelectorViewModel
import p2pdops.dopsender.utils.Constants
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.utils.shrink
import java.io.File

class MediaSelectorsActivity : AppCompatActivity() {

    companion object {
        const val DATA_FILE_TYPE = "DATA_FILE_TYPE"
        private const val TAG = "MediaSelectorsActivity"
    }


    lateinit var fileType: FileType
    private lateinit var viewModel: MediaSelectorViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_selectors)
        viewModel = ViewModelProvider(this).get(MediaSelectorViewModel::class.java)
        val typeValue = intent.getStringExtra(DATA_FILE_TYPE) ?: FileType.Documents.name

        fileType = FileType.valueOf(typeValue)

        supportActionBar?.title = "Your " + simpleType()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Log.d(TAG, "onCreate: $fileType")

        val handler = Handler(Looper.getMainLooper()) {

            val folders = (it.obj as Array<File>)

            view_pager.adapter = MediaSelectorPagerAdapter(
                fileType.name, folders, supportFragmentManager
            )

            val listener = object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    this@MediaSelectorsActivity.currentPosition = position
                    Log.d(TAG, "onPageSelected: ${allSelectedPositions.contains(position)}")
                    invalidateOptionsMenu()
                    bottom_app_bar.performShow()
                }
            }

            view_pager.addOnPageChangeListener(listener)
            tabs.setupWithViewPager(view_pager)

            send_fab.setOnClickListener {
                viewModel.selectedFiles.value?.let { selectedFiles: ArrayList<File> ->
                    val returnIntent = Intent()
                    returnIntent.putExtra(Constants.FILES, selectedFiles)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }

            no_files.shrink()
            true
        }

        Thread {
            val hashSet = ArrayList<File>()

            val lUri: Uri = getFilesUri(fileType)

            val lProjection = arrayOf(MediaStore.Files.FileColumns.DATA)

            val cursor = contentResolver.query(
                lUri, lProjection, getFilesSelection(fileType), null, "date_modified DESC"
            )

            cursor?.let {
                var childFile: File
                val dataIndex: Int = cursor.getColumnIndexOrThrow("_data")

                while (cursor.moveToNext()) {
                    childFile = File(cursor.getString(dataIndex))
                    childFile.parentFile?.let {
                        if (it.exists()) if (!hashSet.contains(it))
                            hashSet.add(it)
                    }
                }

                cursor.close()

                val array =
                    hashSet.toTypedArray()//.reversedArray()//.sortedWith(compareByDescending { it.lastModified()}).toTypedArray()
                handler.obtainMessage(0, array).sendToTarget()
            }
        }.start()

        viewModel.selectedFiles.observe(this) {
            selectedFilesList = it
            CoroutineScope(Dispatchers.Default).launch { updateFooter(it) }
        }
    }

    var selectedFilesList: ArrayList<File> = ArrayList()


    // data manipulation
    fun selectFile(file: File) {
        viewModel.selectFile(file)
    }

    fun deSelectFile(file: File) {
        viewModel.deSelectFile(file)
    }

    fun selectFiles(files: List<File>, position: Int) {
        if (!allSelectedPositions.contains(position))
            allSelectedPositions.add(position)
        viewModel.selectFiles(files)
        viewModel.allSelectedChanged()
    }

    fun deSelectFiles(files: List<File>, position: Int) {
        if (allSelectedPositions.contains(position))
            allSelectedPositions.remove(position)
        viewModel.deSelectFiles(files)
        viewModel.allSelectedChanged()
    }


    // menu
    private var menu: Menu? = null
    private var currentPosition = 0
    private val allSelectedPositions = ArrayList<Int>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: ")
        super.onCreateOptionsMenu(menu)
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
            else -> false
        }
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


    // Utils and UI
    private var totalSize: Long = 0L
    private fun updateFooter(list: ArrayList<File>) {
        this.totalSize = list.fold(0L) { sum, element -> sum + element.length() }
        CoroutineScope(Dispatchers.Main).launch { updateStatus(list) }
    }

    private fun updateStatus(list: ArrayList<File>) {
        Log.d(TAG, "updateStatus: Bytes:$totalSize, List:$list")
        setBottomBarStatus(list)
        setFabVisibility(list)
    }

    private fun setBottomBarStatus(list: ArrayList<File>) {
        val text = simpleType()
        bottom_app_bar_title.text =
            if (list.isEmpty()) getEmptyText() else "${humanizeBytes(totalSize)} | ${list.size} ${if (list.size == 1) text.substring(
                0,
                text.lastIndex
            ) else text} selected"
    }

    private fun setFabVisibility(list: ArrayList<File>) {
        if (list.isEmpty()) send_fab.hide() else if (send_fab.isOrWillBeHidden) send_fab.show()
    }

    private fun getEmptyText() = "Select ${simpleType()} to continue"

    private fun simpleType() = when (fileType) {
        FileType.Documents -> "documents"
        FileType.Apps -> "apps"
        FileType.Images -> "images"
        FileType.Videos -> "videos"
        FileType.Audios -> "audios"
        FileType.Compressed -> "archives"
    }
}