package p2pdops.dopsender.frags

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import p2pdops.dopsender.R
import p2pdops.dopsender.VideosSelectorActivity

import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.utils.show
import p2pdops.dopsender.viewmodels.VideosViewModel
import java.io.File

class VideosFolderFragment : Fragment() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.videoView)!!
        val checked = itemView.findViewById<ImageView>(R.id.checked)!!
        val vidName = itemView.findViewById<TextView>(R.id.videoName)!!
        val vidSize = itemView.findViewById<TextView>(R.id.videoSize)!!
    }

    companion object {
        private const val TAG = "VideosFolderFragment"

        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"

        private const val ARG_FOLDER_PATH = "ARG_FOLDER_PATH"

        @JvmStatic
        fun newInstance(sectionNumber: Int, folderPath: String): VideosFolderFragment {
            return VideosFolderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putString(ARG_FOLDER_PATH, folderPath)
                }
            }
        }

        val extensions = arrayOf("mp4", "avi", "3gp", "ts", "webm", "mkv", "mov")
    }


    private var mActivity: VideosSelectorActivity? = null
    private lateinit var viewModel: VideosViewModel
    private var videosRecyclerView: RecyclerView? = null
    private var videosAdapter: RecyclerView.Adapter<VideoViewHolder>? = null
    var videosList: List<File> = ArrayList()
    var selectedVideosList: ArrayList<File> = ArrayList()

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mActivity = activity as VideosSelectorActivity
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
        setHasOptionsMenu(true);
        val rootView = inflater.inflate(R.layout.frag_videos, container, false)
        viewModel = ViewModelProvider(this).get(VideosViewModel::class.java)

        viewModel.selectedVideos.observe(this as LifecycleOwner, Observer { t: ArrayList<File> ->
            if (t.containsAll(videosList)) mActivity?.showDeSelectAll()
            else mActivity?.showSelectAll()
            selectedVideosList = t
            Log.d(TAG, "change:: $selectedVideosList")
            videosAdapter?.notifyDataSetChanged()
        })

        videosRecyclerView = rootView.findViewById(R.id.videosRecycler)

        arguments?.getString(ARG_FOLDER_PATH)?.let { FetchFolderVideos(it, this).execute() }

        videosAdapter =
            object : RecyclerView.Adapter<VideoViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
                    return VideoViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
                    )
                }

                override fun getItemCount(): Int = videosList.size

                override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {

                    if (selectedVideosList.contains(videosList[position])) {
                        holder.checked.show()
                    } else {
                        holder.checked.hide()
                    }

                    holder.vidName.text = videosList[position].name
                    holder.vidName.isSelected = true
                    holder.vidSize.text = humanizeBytes(videosList[position].length(), 0)
                    Glide.with(context!!)
                        .load(videosList[position].absolutePath)
                        .placeholder(
                            ColorDrawable(
                                ContextCompat.getColor(
                                    context!!,
                                    R.color.placeholder_gray
                                )
                            )
                        )
                        .centerCrop()
                        .thumbnail(0.5f)
                        .into(holder.imageView)
                    holder.itemView.setOnClickListener {
                        if (!selectedVideosList.contains(videosList[position])) {
                            viewModel.addSelectedVideo(
                                videosList[position]
                            )
                            mActivity!!.selectVideo(videosList[position])
                            holder.checked.show()
                        } else {
                            viewModel.removeSelectedVideo(videosList[position])
                            mActivity!!.deSelectVideo(videosList[position])
                            holder.checked.hide()
                        }
                    }
                }

            }

        videosRecyclerView!!.setHasFixedSize(true)

        videosRecyclerView!!.layoutManager = GridLayoutManager(context, 2)

        videosRecyclerView?.adapter = videosAdapter

        return rootView
    }

    class FetchFolderVideos(
        private val folderPath: String,
        private val fragment: VideosFolderFragment
    ) : AsyncTask<Void, Void, List<File>>() {
        override fun doInBackground(vararg params: Void?): List<File> {

            val folder = File(folderPath)

            val files = folder.listFiles()!!.filter { file -> extensions.contains(file.extension) }
                .sortedWith(Comparator { o1, o2 ->
                    if (o1.lastModified() < o2.lastModified()) 1 else -1
                })

            Log.d(TAG, "onCreate: ${folder.name}, files: $files")

            return files
        }

        override
        fun onPostExecute(result: List<File>) {
            super.onPostExecute(result)
            fragment.videosList = result
            fragment.notifyVideosAvailable()
        }
    }

    private fun notifyVideosAvailable() {
        Log.d(TAG, "notifyVideosAvailable: ${videosList.size}")
        videosAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handler = Handler(mActivity)
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {
            R.id.select_all_grp -> {
                handler.post {
                    Log.d(TAG, "onOptionsItemSelected: select")
                    mActivity!!.selectVideosAll(videosList, position).also {
                        viewModel.selectedAllVideos(videosList)
                        mActivity!!.showDeSelectAll()
                    }
                }

                true
            }
            R.id.deselect_all_grp -> {
                handler.post {
                    Log.d(TAG, "onOptionsItemSelected: deselect")
                    mActivity!!.deSelectVideosAll(videosList, position).also {
                        mActivity!!.showSelectAll()
                    }.also {
                        viewModel.deSelectVideoAll(videosList)
                    }
                }
                true
            }

            else -> {
                false
            }
        }
    }
}