package p2pdops.dopsender.selectors.ui.media.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import p2pdops.dopsender.utils.bulge
import p2pdops.dopsender.utils.shrink
import java.io.File

open class BaseMediaHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    open var check: View? = null
    open var checked = false
        set(value) {
            check?.apply { if (value) bulge() else shrink() }
            field = value
        }

    open fun setFile(file: File) {}

    open fun onClick(
        clickWhenChecked: () -> Unit,
        clickWhenUnChecked: () -> Unit
    ) = itemView.setOnClickListener {
        checked = !checked
        (if (checked) clickWhenChecked else clickWhenUnChecked)()
    }
}
