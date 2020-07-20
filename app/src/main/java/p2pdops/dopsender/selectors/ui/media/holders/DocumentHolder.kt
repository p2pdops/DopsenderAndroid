package p2pdops.dopsender.selectors.ui.media.holders

import android.graphics.PorterDuff
import android.view.View
import android.widget.TextView

import androidx.core.content.ContextCompat
import p2pdops.dopsender.R
import p2pdops.dopsender.views.SquareImage
import java.io.File

import kotlinx.android.synthetic.main.item_doc.view.*
import p2pdops.dopsender.utils.*
import java.util.*

class DocumentHolder(itemView: View) : BaseMediaHolder(itemView) {

    override var check: View? = itemView.documentChecked
    private val icon: SquareImage = itemView.docIcon
    private val name: TextView = itemView.docName
    private val type: TextView = itemView.docType
    private val size: TextView = itemView.docSize

    override fun setFile(file: File) {

        file.extension.toUpperCase(Locale.getDefault()).apply {
            if (this != "") type.text = this
            else type.hide()
        }

        name.text = file.name
        name.isSelected = true
        size.text = humanizeBytes(file.length(), 0)

        ContextCompat.getColor(
            itemView.context,
            docsColormap.getOrElse(file.extension) { R.color.color_pdf })
            .apply {
                icon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
                type.setTextColor(this)
                size.setTextColor(this)
            }
    }
}