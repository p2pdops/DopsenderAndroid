package p2pdops.dopsender.frags
//
//import android.app.Activity
//import android.graphics.PorterDuff
//import android.os.AsyncTask
//import android.os.Bundle
//import android.os.Handler
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import p2pdops.dopsender.CompressedSelectorActivity
//
//import p2pdops.dopsender.R
//
//import p2pdops.dopsender.utils.hide
//import p2pdops.dopsender.utils.humanizeBytes
//import p2pdops.dopsender.utils.show
//import p2pdops.dopsender.viewmodels.CompressedViewModel
//
//import java.io.File
//import java.util.*
//import kotlin.Comparator
//import kotlin.collections.ArrayList
//
//
//class CompressedFolderFragment : Fragment() {
//    val map = mapOf(
//        "zip" to R.color.color_zip,
//        "rar" to R.color.color_rar,
//        "obb" to R.color.color_rar
//    )
//
//    class CompressedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val checked = itemView.findViewById<ImageView>(R.id.checked)!!
//        val compressedName = itemView.findViewById<TextView>(R.id.compressedName)!!
//        val compressedType = itemView.findViewById<TextView>(R.id.compressedType)!!
//        val compressedSize = itemView.findViewById<TextView>(R.id.compressedSize)!!
//        val compressedIcon =
//            itemView.findViewById<ImageView>(R.id.compressedIcon)!!
//    }
//
//    companion object {
//        private const val TAG = "CompressedFragment"
//
//        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"
//
//        private const val ARG_FOLDER_PATH = "ARG_FOLDER_PATH"
//
//        @JvmStatic
//        fun newInstance(sectionNumber: Int, folderPath: String): CompressedFolderFragment {
//            return CompressedFolderFragment().apply {
//                arguments = Bundle().apply {
//                    putInt(ARG_SECTION_NUMBER, sectionNumber)
//                    putString(ARG_FOLDER_PATH, folderPath)
//                }
//            }
//        }
//
//        val extensions = arrayOf("zip", "rar", "obb")
//    }
//
//
//    private lateinit var viewModel: CompressedViewModel
//    private var mActivity: CompressedSelectorActivity? = null
//    private var compressedRecyclerView: RecyclerView? = null
//    private var compressedAdapter: RecyclerView.Adapter<CompressedViewHolder>? = null
//    var compressedList: List<File> = ArrayList()
//    var selectedCompressedList: ArrayList<File> = ArrayList()
//
//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        try {
//            mActivity = activity as CompressedSelectorActivity
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
//        val rootView = inflater.inflate(R.layout.frag_compressed, container, false)
//        viewModel = ViewModelProvider(this).get(CompressedViewModel::class.java)
//
//        viewModel.selectedCompressed.observe(
//            this as LifecycleOwner,
//            Observer { t: ArrayList<File> ->
//                if (t.containsAll(compressedList)) mActivity?.showDeSelectAll()
//                else mActivity?.showSelectAll()
//                selectedCompressedList = t
//                compressedAdapter?.notifyDataSetChanged()
//                Log.d(TAG, "change:: $selectedCompressedList")
//            })
//
//        compressedRecyclerView = rootView.findViewById(R.id.compressedRecycler)
//
//        arguments?.getString(ARG_FOLDER_PATH)?.let { FetchFolderCompressed(it, this).execute() }
//
//        compressedAdapter =
//            object : RecyclerView.Adapter<CompressedViewHolder>() {
//                override fun onCreateViewHolder(
//                    parent: ViewGroup,
//                    viewType: Int
//                ): CompressedViewHolder {
//                    return CompressedViewHolder(
//                        LayoutInflater.from(context)
//                            .inflate(R.layout.item_compressed, parent, false)
//                    )
//                }
//
//                override fun getItemCount(): Int = compressedList.size
//
//                override fun onBindViewHolder(holder: CompressedViewHolder, position: Int) {
//
//                    Log.d(TAG, "onBindViewHolder: ${compressedList[position].name}")
//                    if (selectedCompressedList.contains(compressedList[position])) {
//                        holder.checked.show()
//                    } else {
//                        holder.checked.hide()
//                    }
//
//                    holder.compressedName.text = compressedList[position].name
//                    holder.compressedName.isSelected = true
//                    holder.compressedSize.text = humanizeBytes(compressedList[position].length(), 0)
//                    compressedList[position].extension.toUpperCase(Locale.getDefault()).apply {
//                        if (this != "") holder.compressedType.text = this
//                        else holder.compressedType.hide()
//                    }
//
//                    ContextCompat.getColor(
//                        context!!,
//                        map.getOrElse(compressedList[position].extension) { R.color.color_pdf })
//                        .apply {
//                            holder.compressedIcon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
//                            holder.compressedType.setTextColor(this)
//                            holder.compressedSize.setTextColor(this)
//                        }
//
//                    holder.itemView.setOnClickListener {
//                        if (!selectedCompressedList.contains(compressedList[position])) {
//                            viewModel.addSelectedAudio(
//                                compressedList[position]
//                            )
//                            mActivity!!.selectCompressed(compressedList[position])
//                            holder.checked.show()
//                        } else {
//                            viewModel.removeSelectedAudio(compressedList[position])
//                            mActivity!!.deSelectCompressed(compressedList[position])
//                            holder.checked.hide()
//                        }
//                    }
//                }
//
//            }
//
//        compressedRecyclerView!!.setHasFixedSize(true)
//
//        compressedRecyclerView!!.layoutManager = GridLayoutManager(context, 2)
//
//        compressedRecyclerView?.adapter = compressedAdapter
//
//        return rootView
//    }
//
//    class FetchFolderCompressed(
//        private val folderPath: String,
//        private val fragment: CompressedFolderFragment
//    ) : AsyncTask<Void, Void, List<File>>() {
//        override fun doInBackground(vararg params: Void?): List<File> {
//
//            val folder = File(folderPath)
//
//            val files = folder.listFiles()!!.filter { file -> extensions.contains(file.extension) }
//                .sortedWith(Comparator { o1, o2 ->
//                    if (o1.lastModified() < o2.lastModified()) 1 else -1
//                })
//
//            Log.d(TAG, "onCreate: ${folder.name}, files: $files")
//
//            return files
//        }
//
//        override
//        fun onPostExecute(result: List<File>) {
//            super.onPostExecute(result)
//            fragment.compressedList = result
//            fragment.notifyCompressedAvailable()
//        }
//    }
//
//    private fun notifyCompressedAvailable() {
//        Log.d(TAG, "notifyCompressedAvailable: ${compressedList.size}")
//        compressedAdapter?.notifyDataSetChanged()
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val handler = Handler(mActivity)
//        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
//        return when (item.itemId) {
//            R.id.select_all_grp -> {
//                handler.post {
//                    Log.d(TAG, "onOptionsItemSelected: select")
//                    mActivity!!.selectCompressedAll(compressedList, position).also {
//                        viewModel.selectedAllCompressed(compressedList)
//                        mActivity!!.showDeSelectAll()
//                    }
//                }
//
//                true
//            }
//            R.id.deselect_all_grp -> {
//                handler.post {
//                    Log.d(TAG, "onOptionsItemSelected: deselect")
//                    mActivity!!.deSelectCompressedAll(compressedList, position).also {
//                        mActivity!!.showSelectAll()
//                    }.also {
//                        viewModel.deSelectAllCompressed(compressedList)
//                    }
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