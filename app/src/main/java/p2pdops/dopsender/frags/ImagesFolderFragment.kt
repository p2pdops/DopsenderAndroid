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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import p2pdops.dopsender.ImagesSelectorActivity
import p2pdops.dopsender.R
import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.show
import p2pdops.dopsender.viewmodels.ImagesViewModel
import java.io.File

class ImagesFolderFragment : Fragment() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)!!
        val checked = itemView.findViewById<ImageView>(R.id.checked)!!
    }

    companion object {
        private const val TAG = "ImagesFolderFragment"

        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"

        private const val ARG_FOLDER_PATH = "ARG_FOLDER_PATH"

        @JvmStatic
        fun newInstance(sectionNumber: Int, folderPath: String): ImagesFolderFragment {
            return ImagesFolderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putString(ARG_FOLDER_PATH, folderPath)
                }
            }
        }

        val exts = arrayOf("jpg", "jpeg", "png")
    }


    private var mActivity: ImagesSelectorActivity? = null
    private lateinit var viewModel: ImagesViewModel
    private var imagesRecyclerView: RecyclerView? = null
    private var imagesAdapter: RecyclerView.Adapter<ImageViewHolder>? = null
    var imagesList: List<File> = ArrayList()
    var selectedImagesList: ArrayList<File> = ArrayList()

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mActivity = activity as ImagesSelectorActivity
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
        val rootView = inflater.inflate(R.layout.frag_images, container, false)
        viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)

        viewModel.selectedImages.observe(this as LifecycleOwner, Observer { t: ArrayList<File> ->
            if (t.containsAll(imagesList)) mActivity?.showDeSelectAll()
            else mActivity?.showSelectAll()
            selectedImagesList = t
            Log.d(TAG, "change:: $selectedImagesList")
            imagesAdapter?.notifyDataSetChanged()
        })

        imagesRecyclerView = rootView.findViewById(R.id.imagesRecycler)

        arguments?.getString(ARG_FOLDER_PATH)?.let { FetchFolderImages(it, this).execute() }

        imagesAdapter =
            object : RecyclerView.Adapter<ImageViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
                    return ImageViewHolder(
                        LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
                    )
                }

                override fun getItemCount(): Int = imagesList.size

                override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

                    if (selectedImagesList.contains(imagesList[position])) {
                        holder.checked.show()
                    } else {
                        holder.checked.hide()
                    }

                    Glide.with(context!!)
                        .load(imagesList[position].absolutePath)
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
                        if (!selectedImagesList.contains(imagesList[position])) {
                            viewModel.addSelectedImage(
                                imagesList[position]
                            )
                            mActivity!!.selectImage(imagesList[position])
                            holder.checked.show()
                        } else {
                            viewModel.removeSelectedImage(imagesList[position])
                            mActivity!!.deSelectImage(imagesList[position])
                            holder.checked.hide()
                        }
                    }
                }

            }

        imagesRecyclerView!!.setHasFixedSize(true)

        imagesRecyclerView!!.layoutManager = GridLayoutManager(context, 3)

        imagesRecyclerView?.adapter = imagesAdapter

        return rootView
    }

    class FetchFolderImages(
        private val folderPath: String,
        private val fragment: ImagesFolderFragment
    ) : AsyncTask<Void, Void, List<File>>() {
        override fun doInBackground(vararg params: Void?): List<File> {

            val folder = File(folderPath)

            val files = folder.listFiles()!!.filter { file -> exts.contains(file.extension) }
                .sortedWith(Comparator { o1, o2 ->
                    if (o1.lastModified() < o2.lastModified()) 1 else -1
                })

            Log.d(TAG, "onCreate: ${folder.name}, files: $files")

            return files
        }

        override
        fun onPostExecute(result: List<File>) {
            super.onPostExecute(result)
            fragment.imagesList = result
            fragment.notifyImagesAvailable()
        }
    }

    private fun notifyImagesAvailable() {
        Log.d(TAG, "notifyImagesAvailable: ${imagesList.size}")
        imagesAdapter?.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handler = Handler(mActivity)
        val position = arguments?.getInt(ARG_SECTION_NUMBER)!!
        return when (item.itemId) {

            R.id.select_all_grp -> {

                handler.post {
                    Log.d(TAG, "onOptionsItemSelected: select")
                    mActivity!!.selectImageAll(imagesList, position).also {
                        viewModel.selectedImages(imagesList)
                        mActivity!!.showDeSelectAll()
                    }
                }

                true
            }

            R.id.deselect_all_grp -> {
                handler.post {
                    Log.d(TAG, "onOptionsItemSelected: deselect")
                    mActivity!!.deSelectImageAll(imagesList, position).also {
                        mActivity!!.showSelectAll()
                    }.also {
                        viewModel.deSelectImageAll(imagesList)
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