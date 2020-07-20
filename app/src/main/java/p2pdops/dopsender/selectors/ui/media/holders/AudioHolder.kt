package p2pdops.dopsender.selectors.ui.media.holders

import android.view.View
import android.widget.TextView

import java.io.File

import kotlinx.android.synthetic.main.item_audio.view.*
import p2pdops.dopsender.utils.humanizeBytes

class AudioHolder(itemView: View) : BaseMediaHolder(itemView) {

    override var check: View? = itemView.audioChecked
    private val name: TextView = itemView.audioName
    private val size: TextView = itemView.audioSize

    override fun setFile(file: File) {
        name.text = file.name
        name.isSelected = true
        size.text = humanizeBytes(file.length(), 0)
    }
}