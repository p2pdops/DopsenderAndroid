package p2pdops.dopsender.selectors.ui.media.holders

import android.graphics.PorterDuff
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import p2pdops.dopsender.R
import p2pdops.dopsender.utils.hide
import p2pdops.dopsender.utils.humanizeBytes
import p2pdops.dopsender.views.SquareImage
import java.io.File
import java.util.*

import kotlinx.android.synthetic.main.item_compressed.view.*

class CompressedHolder(itemView: View) : BaseMediaHolder(itemView) {

    private val colorMap = mapOf(
        "zip" to R.color.color_zip,
        "rar" to R.color.color_rar,
        "obb" to R.color.color_rar
    )

    override var check: View? = itemView.compressedChecked
    private val icon: SquareImage = itemView.compressedIcon
    private val name: TextView = itemView.compressedName
    private val size: TextView = itemView.compressedSize
    private val type: TextView = itemView.compressedType


    override fun setFile(file: File) {
        name.text = file.name
        name.isSelected = true
        size.text = humanizeBytes(file.length(), 0)
        file.extension.toUpperCase(Locale.getDefault()).apply {
            if (this != "") type.text = this
            else type.hide()
        }

        ContextCompat.getColor(
            itemView.context,
            colorMap.getOrElse(file.extension) { R.color.color_pdf })
            .apply {
                icon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
                type.setTextColor(this)
                size.setTextColor(this)
            }
    }

}