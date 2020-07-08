package p2pdops.dopsender.frags

import android.app.Activity
import android.graphics.PorterDuff
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
import p2pdops.dopsender.DocsSelectorActivity

import p2pdops.dopsender.R
import p2pdops.dopsender.utils.docsColormap

import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.utils.show
import p2pdops.dopsender.viewmodels.DocsViewModel

import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class DocsFolderFragment : Fragment() {

    class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checked = itemView.findViewById<ImageView>(R.id.checked)!!
        val docName = itemView.findViewById<TextView>(R.id.docName)!!
        val docType = itemView.findViewById<TextView>(R.id.docType)!!
        val docSize = itemView.findViewById<TextView>(R.id.docSize)!!
        val docIcon: ImageView = itemView.findViewById<View>(R.id.docIcon) as ImageView

    }

    companion object {
        private const val TAG = "DocsFolderFragment"

        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"

        private const val ARG_FOLDER_PATH = "ARG_FOLDER_PATH"

        @JvmStatic
        fun newInstance(sectionNumber: Int, folderPath: String): DocsFolderFragment {
            return DocsFolderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putString(ARG_FOLDER_PATH, folderPath)
                }
            }
        }

        val extensions = arrayOf("pdf", "csv", "doc", "docx", "xls", "xlsx")
    }


    private lateinit var viewModel: DocsViewModel
    private var mActivity: DocsSelectorActivity? = null
    private var docsRecyclerView: RecyclerView? = null
    private var docsAdapter: RecyclerView.Adapter<DocsViewHolder>? = null
    var docsList: List<File> = ArrayList()
    var selectedDocsList: ArrayList<File> = ArrayList()

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mActivity = activity as DocsSelectorActivity
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
        val rootView = inflater.inflate(R.layout.frag_docs, container, false)
        viewModel = ViewModelProvider(this).get(DocsViewModel::class.java)

        viewModel.selectedDocs.observe(this as LifecycleOwner, Observer { t: ArrayList<File> ->
            if (t.containsAll(docsList)) mActivity?.showDeSelectAll()
            else mActivity?.showSelectAll()
            selectedDocsList = t
            Log.d(TAG, "change:: $selectedDocsList")
            docsAdapter?.notifyDataSetChanged()
        })

        docsRecyclerView = rootView.findViewById(R.id.docsRecycler)

        arguments?.getString(ARG_FOLDER_PATH)?.let { FetchFolderDocs(it, this).execute() }

        docsAdapter =
            object : RecyclerView.Adapter<DocsViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocsViewHolder {
                    return DocsViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.item_doc, parent, false)
                    )
                }

                override fun getItemCount(): Int = docsList.size

                override fun onBindViewHolder(holder: DocsViewHolder, position: Int) {

                    if (selectedDocsList.contains(docsList[position])) {
                        holder.checked.show()
                    } else {
                        holder.checked.hide()
                    }

                    holder.docName.text = docsList[position].name
                    holder.docName.isSelected = true
                    holder.docSize.text = humanizeBytes(docsList[position].length(), 0)

                    docsList[position].extension.toUpperCase(Locale.getDefault()).apply {
                        if (this != "") holder.docType.text = this
                        else holder.docType.hide()
                    }

                    ContextCompat.getColor(
                        context!!,
                        docsColormap.getOrElse(docsList[position].extension) { R.color.color_pdf })
                        .apply {
                            holder.docIcon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
                            holder.docType.setTextColor(this)
                            holder.docSize.setTextColor(this)
                        }

                    holder.itemView.setOnClickListener {
                        if (!selectedDocsList.contains(docsList[position])) {
                            viewModel.addSelectedAudio( docsList[position])
                            mActivity!!.selectDoc(docsList[position])
                            holder.checked.show()
                        } else {
                            viewModel.removeSelectedAudio(docsList[position])
                            mActivity!!.deSelectDoc(docsList[position])
                            holder.checked.hide()
                        }
                    }
                }

            }

        docsRecyclerView!!.setHasFixedSize(true)

        docsRecyclerView!!.layoutManager = GridLayoutManager(context, 2)

        docsRecyclerView?.adapter = docsAdapter

        return rootView
    }

    class FetchFolderDocs(
        private val folderPath: String,
        private val fragment: DocsFolderFragment
    ) : AsyncTask<Void, Void, List<File>>() {
        override fun doInBackground(vararg params: Void?): List<File> {

            val folder = File(folderPath)

            val files =
                folder.listFiles()!!.filter { file -> extensions.contains(file.extension) }
                    .sortedWith(Comparator { o1, o2 ->
                        if (o1.lastModified() < o2.lastModified()) 1 else -1
                    })

            Log.d(TAG, "onCreate: ${folder.name}, files: $files")

            return files
        }

        override
        fun onPostExecute(result: List<File>) {
            super.onPostExecute(result)
            fragment.docsList = result
            fragment.notifyDocsAvailable()
        }
    }

    private fun notifyDocsAvailable() {
        Log.d(TAG, "notifyDocsAvailable: ${docsList.size}")
        docsAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {
            R.id.select_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: select")
                mActivity!!.selectDocAll(docsList, position).also {
                    viewModel.selectedAllDocs(docsList)
                    mActivity!!.showDeSelectAll()
                }


                true
            }
            R.id.deselect_all_grp -> {
                Log.d(TAG, "onOptionsItemSelected: deselect")
                mActivity!!.deSelectDocAll(docsList, position).also {
                    mActivity!!.showSelectAll()
                }.also {
                    viewModel.deSelectAllDocs(docsList)
                }

                true
            }

            else -> {
                false
            }
        }
    }
}