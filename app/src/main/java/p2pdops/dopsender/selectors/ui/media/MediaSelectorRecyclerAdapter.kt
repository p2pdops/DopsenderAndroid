package p2pdops.dopsender.selectors.ui.media

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.selectors.MediaSelectorsActivity
import p2pdops.dopsender.selectors.ui.media.holders.*
import java.io.File

class MediaSelectorRecyclerAdapter(
    private val activity: MediaSelectorsActivity,
    private val filesList: List<File>
) : RecyclerView.Adapter<BaseMediaHolder>() {

    override fun getItemCount(): Int = filesList.size

    override fun onCreateViewHolder(
        p: ViewGroup,
        viewType: Int
    ): BaseMediaHolder = when (activity.fileType) {
        FileType.Compressed -> CompressedHolder(l(R.layout.item_compressed, p))
        FileType.Documents -> DocumentHolder(l(R.layout.item_doc, p))
        FileType.Audios -> AudioHolder(l(R.layout.item_audio, p))
        FileType.Images -> ImageHolder(l(R.layout.item_image, p))
        FileType.Videos -> VideoHolder(l(R.layout.item_video, p))
        FileType.Apps -> BaseMediaHolder(l(0, p))
    }

    override fun onBindViewHolder(holder: BaseMediaHolder, position: Int) {
        val currentFile = filesList[position]
        val isSelected = activity.selectedFilesList.contains(currentFile)
        holder.setFile(currentFile)
        holder.checked = isSelected

        holder.onClick(
            { activity.selectFile(currentFile) },
            { activity.deSelectFile(currentFile) }
        )
    }

    private fun l(res: Int, p: ViewGroup): View = activity.layoutInflater.inflate(res, p, false)
}