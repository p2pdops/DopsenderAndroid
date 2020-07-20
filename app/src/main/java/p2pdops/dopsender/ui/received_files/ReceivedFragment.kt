package p2pdops.dopsender.ui.received_files

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_received.view.*
import kotlinx.android.synthetic.main.received_file.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import p2pdops.dopsender.BuildConfig
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.utils.*
import java.io.File


class ReceivedFragment : Fragment() {

    private lateinit var folderType: FileType
    private var files: Array<File> = arrayOf()

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val TAG = "ReceivedFragment"

        @JvmStatic
        fun newInstance(sectionNumber: Int): ReceivedFragment {
            return ReceivedFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private fun fetchFiles(emptyCallback: () -> Unit, filesCallback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            arguments?.getInt(ARG_SECTION_NUMBER)?.let { position ->
                this@ReceivedFragment.folderType = fileTypeList[position]
                (requireActivity().getDopsenderFolder().canonicalPath + '/' + folderType.name + "/").apply {
                    val folder = File(this)
                    if (folder.exists() || folder.mkdir()) {
                        this@ReceivedFragment.files = folder.listFiles() ?: arrayOf()
                        Log.d(TAG, "fetchFiles: ${Gson().toJson(files)}")
                        CoroutineScope(Dispatchers.Main).launch {
                            filesCallback()
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            emptyCallback()
                        }
                    }
                }
            }
        }
    }

    class ReceivedFile(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_received, container, false)
        root.receivedFilesRecycler.setHasFixedSize(true)
        fetchFiles({}, {
            if (files.isNotEmpty())
                root.noFiles.shrink()
            root.receivedFilesRecycler.layoutManager = GridLayoutManager(context, 2)
            root.receivedFilesRecycler.adapter = object : RecyclerView.Adapter<ReceivedFile>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedFile =
                    ReceivedFile(inflater.inflate(R.layout.received_file, parent, false))

                override fun getItemCount(): Int = files.size

                override fun onBindViewHolder(holder: ReceivedFile, position: Int) {
                    val currFile = files[position]
                    val imageView = holder.itemView.imageView
                    holder.itemView.nameView.text = currFile.name
                    holder.itemView.sizeView.text = humanizeBytes(currFile.length())


                    holder.itemView.setOnClickListener {
                        try {
                            if (folderType == FileType.Apps) {
                                ApkInstaller.installApplication(requireContext(), currFile)
                            } else {
                                val uri: Uri = FileProvider.getUriForFile(
                                    requireContext(),
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    currFile
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                if (folderType != FileType.Apps)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(intent)
                            }

                        } catch (e: ActivityNotFoundException) {
                            Log.e(TAG, "onBindViewHolder:", e)
                            // no Activity to handle this kind of files
                        }

                    }

                    when (folderType) {
                        FileType.Documents -> {
                            imageView.setColorFilter(
                                AppCompatResources.getColorStateList(
                                    imageView.context,
                                    docsColormap.getOrElse(currFile.extension) { R.color.c_doc }
                                ).defaultColor, PorterDuff.Mode.SRC_IN
                            )
                            imageView.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    imageView.context,
                                    R.drawable.ic_compressed
                                )
                            )
                        }
                        FileType.Apps -> {
                            val pm = context?.packageManager
                            pm?.getPackageArchiveInfo(
                                currFile.absolutePath,
                                PackageManager.GET_META_DATA
                            )?.let {
                                it.applicationInfo.sourceDir = currFile.absolutePath
                                it.applicationInfo.publicSourceDir = currFile.absolutePath
                                Log.d("ConnReceiveHolder", "appInfo: ${it.applicationInfo}")
                                Glide.with(this@ReceivedFragment)
                                    .load(it.applicationInfo.loadIcon(pm))
                                    .error(R.drawable.ic_apk)
                                    .thumbnail(0.2f)
                                    .centerCrop()
                                    .into(imageView)
                            }!!
                        }
                        FileType.Images, FileType.Videos -> {
                            Glide.with(this@ReceivedFragment).load(currFile.absolutePath)
                                .thumbnail(0.5f)
                                .centerCrop()
                                .error(R.drawable.ic_images).into(imageView)
                        }
                        FileType.Audios -> {
                            imageView.setColorFilter(
                                AppCompatResources.getColorStateList(
                                    imageView.context,
                                    R.color.c_aud
                                ).defaultColor, PorterDuff.Mode.SRC_IN
                            )
                            imageView.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    imageView.context,
                                    R.drawable.ic_audio_outline
                                )
                            )
                        }
                        FileType.Compressed -> {
                            imageView.setColorFilter(
                                AppCompatResources.getColorStateList(
                                    imageView.context,
                                    R.color.c_com
                                ).defaultColor, PorterDuff.Mode.SRC_IN
                            )
                            imageView.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    imageView.context,
                                    R.drawable.ic_compressed
                                )
                            )
                        }
                    }
                }

            }
        })
        return root
    }
}