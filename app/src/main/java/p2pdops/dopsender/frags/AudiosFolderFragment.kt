package p2pdops.dopsender.frags
//
//import android.app.Activity
//import android.os.AsyncTask
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
//import p2pdops.dopsender.R
//
//
//import p2pdops.dopsender.utils.hide
//import p2pdops.dopsender.utils.humanizeBytes
//import p2pdops.dopsender.utils.show
//import p2pdops.dopsender.viewmodels.AudiosViewModel
//
//import java.io.File
//
//class AudiosFolderFragment : Fragment() {
//
//    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val checked = itemView.findViewById<ImageView>(R.id.checked)!!
//        val audName = itemView.findViewById<TextView>(R.id.audioName)!!
//        val audSize = itemView.findViewById<TextView>(R.id.audioSize)!!
//    }
//
//    companion object {
//        private const val TAG = "AudiosFolderFragment"
//
//        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"
//
//        private const val ARG_FOLDER_PATH = "ARG_FOLDER_PATH"
//
//        @JvmStatic
//        fun newInstance(sectionNumber: Int, folderPath: String): AudiosFolderFragment {
//            return AudiosFolderFragment().apply {
//                arguments = Bundle().apply {
//                    putInt(ARG_SECTION_NUMBER, sectionNumber)
//                    putString(ARG_FOLDER_PATH, folderPath)
//                }
//            }
//        }
//
//    }
//
//
//    private var mActivity: AudiosSelectorActivity? = null
//    private lateinit var viewModel: AudiosViewModel
//    private var audiosRecyclerView: RecyclerView? = null
//    private var audiosAdapter: RecyclerView.Adapter<AudioViewHolder>? = null
//    var audiosList: List<File> = ArrayList()
//    var selectedAudiosList: ArrayList<File> = ArrayList()
//
//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        try {
//            mActivity = activity as AudiosSelectorActivity
//        } catch (e: ClassCastException) {
//            throw ClassCastException(
//                "$activity must implement onSomeEventListener"
//            )
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        setHasOptionsMenu(true)
//        val rootView = inflater.inflate(R.layout.frag_audios, container, false)
//        viewModel = ViewModelProvider(this).get(AudiosViewModel::class.java)
//
//        viewModel.selectedAudios.observe(this as LifecycleOwner, Observer { t: ArrayList<File> ->
//            if (t.containsAll(audiosList)) mActivity?.showDeSelectAll()
//            else mActivity?.showSelectAll()
//            selectedAudiosList = t
//            audiosAdapter?.notifyDataSetChanged()
//            Log.d(TAG, "change:: $selectedAudiosList")
//        })
//
//        audiosRecyclerView = rootView.findViewById(R.id.audiosRecycler)
//
//        arguments?.getString(ARG_FOLDER_PATH)?.let { FetchFolderAudios(it, this).execute() }
//
//        audiosAdapter =
//            object : RecyclerView.Adapter<AudioViewHolder>() {
//                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
//                    return AudioViewHolder(
//                        LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
//                    )
//                }
//
//                override fun getItemCount(): Int = audiosList.size
//
//                override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
//
//                    if (selectedAudiosList.contains(audiosList[position])) {
//                        holder.checked.show()
//                    } else {
//                        holder.checked.hide()
//                    }
//
//                    holder.audName.text = audiosList[position].name
//                    holder.audName.isSelected = true
//                    holder.audSize.text = humanizeBytes(audiosList[position].length(), 0)
//
//                    holder.itemView.setOnClickListener {
//                        if (!selectedAudiosList.contains(audiosList[position])) {
//                            viewModel.addSelectedAudio(
//                                audiosList[position]
//                            )
//                            mActivity!!.selectAudio(audiosList[position])
//                            holder.checked.show()
//                        } else {
//                            viewModel.removeSelectedAudio(audiosList[position])
//                            mActivity!!.deSelectAudio(audiosList[position])
//                            holder.checked.hide()
//                        }
//                    }
//                }
//
//            }
//
//        audiosRecyclerView!!.setHasFixedSize(true)
//
//        audiosRecyclerView!!.layoutManager = GridLayoutManager(context, 2)
//
//        audiosRecyclerView?.adapter = audiosAdapter
//
//        return rootView
//    }
//
//    class FetchFolderAudios(
//        private val folderPath: String,
//        private val fragment: AudiosFolderFragment
//    ) : AsyncTask<Void, Void, List<File>>() {
//        override fun doInBackground(vararg params: Void?): List<File> {
//
//            val folder = File(folderPath)
//
//            val files =
//                folder.listFiles()!!//.filter { file -> extensions.contains(file.extension) }
//                    .sortedWith(Comparator { o1, o2 ->
//                        if (o1.lastModified() < o2.lastModified()) 1 else -1
//                    })
//
//            Log.d(TAG, "onCreate: ${folder.name}, files: $files")
//
//            return files
//        }
//
//        override
//        fun onPostExecute(result: List<File>) {
//            super.onPostExecute(result)
//            fragment.audiosList = result
//            fragment.notifyAudiosAvailable()
//        }
//    }
//
//    private fun notifyAudiosAvailable() {
//        Log.d(TAG, "notifyAudiosAvailable: ${audiosList.size}")
//        audiosAdapter?.notifyDataSetChanged()
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
//        return when (item.itemId) {
//            R.id.select_all_grp -> {
//
//                Log.d(TAG, "onOptionsItemSelected: select")
//                mActivity!!.selectAudioAll(audiosList, position).also {
//                    viewModel.selectedAllAudios(audiosList)
//                    mActivity!!.showDeSelectAll()
//                }
//
//                true
//            }
//            R.id.deselect_all_grp -> {
//                Log.d(TAG, "onOptionsItemSelected: deselect")
//                mActivity!!.deSelectAudioAll(audiosList, position).also {
//                    mActivity!!.showSelectAll()
//                }.also {
//                    viewModel.deSelectAllAudios(audiosList)
//                }
//                true
//            }
//
//            else -> {
//                false
//            }
//        }
//    }
//}