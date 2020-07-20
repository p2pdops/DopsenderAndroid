package p2pdops.dopsender.selectors.ui.media.holders

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import p2pdops.dopsender.R
import p2pdops.dopsender.views.SquareImage
import java.io.File

import kotlinx.android.synthetic.main.item_video.view.*
import p2pdops.dopsender.utils.humanizeBytes

class VideoHolder(itemView: View) : BaseMediaHolder(itemView) {

    override var check: View? = itemView.videoChecked
    private val thumb: SquareImage = itemView.videoView
    private val name: TextView = itemView.videoName
    private val size: TextView = itemView.videoSize

    override fun setFile(file: File) {

        name.text = file.name
        name.isSelected = true
        size.text = humanizeBytes(file.length(), 0)
        Glide.with(itemView)
            .load(file.absolutePath)
            .placeholder(
                ColorDrawable(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.placeholder_gray
                    )
                )
            )
            .centerCrop()
            .thumbnail(0.4f)
            .into(thumb)
    }
}