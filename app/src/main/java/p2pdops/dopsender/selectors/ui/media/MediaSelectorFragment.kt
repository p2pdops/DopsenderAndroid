package p2pdops.dopsender.selectors.ui.media

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_media_selector.view.*
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.selectors.MediaSelectorsActivity
import p2pdops.dopsender.selectors.getExtensionsFilter
import java.io.File

class MediaSelectorFragment : Fragment() {

    private lateinit var selectorsViewModel: MediaSelectorViewModel
    private var fetchFilesThread: Thread? = null
    private var filesAdapter: MediaSelectorRecyclerAdapter? = null
    private var mediaActivity: MediaSelectorsActivity? = null

    private var filesList: List<File> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_media_selector, container, false)

        activity?.also { _activity ->

            mediaActivity = _activity as MediaSelectorsActivity
            selectorsViewModel =
                ViewModelProvider(mediaActivity!!).get(MediaSelectorViewModel::class.java)

            root.filesRecycler.setHasFixedSize(true)

            root.filesRecycler.layoutManager = GridLayoutManager(
                context, when (mediaActivity!!.fileType) {
                    FileType.Images -> 3
                    else -> 2
                }
            )

            fetchFiles(Handler(Looper.getMainLooper(), Handler.Callback {
                filesList = it.obj as List<File>
                filesAdapter = MediaSelectorRecyclerAdapter(
                    mediaActivity as MediaSelectorsActivity,
                    filesList
                )

                root.filesRecycler.adapter = filesAdapter

                true
            }), mediaActivity!!.fileType)

            selectorsViewModel.allSelectionChange.observe(mediaActivity!!) {
                filesAdapter?.notifyDataSetChanged()
            }
        }
        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ")
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {
            R.id.select_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: select")
                mediaActivity?.selectFiles(filesList, position).also {
                    mediaActivity!!.showDeSelectAll()
                    Log.d(TAG, "onOptionsItemSelected: showDeSelectAll")
                }

                true
            }
            R.id.deselect_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: deselect")
                mediaActivity?.deSelectFiles(filesList, position).also {
                    mediaActivity?.showSelectAll()
                }

                true
            }

            else -> {
                false
            }
        }
    }

    private fun fetchFiles(handler: Handler, fileType: FileType) {

        arguments?.getSerializable(ARG_FOLDER_FILE)?.let {
            fetchFilesThread = Thread {
                val folder = it as File
                Log.d(TAG, "${folder.name}: THREAD STARTED")
                try {
                    val files = folder.listFiles()!!.filter { file ->
                        getExtensionsFilter(fileType).contains(file.extension)
                    }.sortedWith(compareByDescending { it.lastModified() }).toList()

                    Log.d(TAG, "${folder.name}: THREAD ENDED")
                    handler.obtainMessage(0, files).sendToTarget()
                } catch (e: Exception) {
                    Log.e(TAG, "onCreateView: $folder", e)
                }
            }

            fetchFilesThread?.start()
        }

    }

    override fun onStop() {
        super.onStop()
        fetchFilesThread?.let {
            if (it.isAlive && !it.isInterrupted) {
                Log.d(TAG, "onStop: interrupting")
                it.interrupt()
            }
        }
    }

    companion object {
        private const val TAG = "MediaSelectorFragment"
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_FOLDER_FILE = "ARG_FOLDER"
        private const val ARG_FILE_TYPE = "ARG_FILE_TYPES"

        @JvmStatic
        fun newInstance(sectionNumber: Int, file: File, fileType: String): MediaSelectorFragment {
            return MediaSelectorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putSerializable(ARG_FOLDER_FILE, file)
                    putString(ARG_FILE_TYPE, fileType)
                }
            }
        }
    }
}