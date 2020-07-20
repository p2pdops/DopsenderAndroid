package p2pdops.dopsender.selectors

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_option.view.*
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import p2pdops.dopsender.selectors.MediaSelectorsActivity.Companion.DATA_FILE_TYPE
import p2pdops.dopsender.utils.*

private const val TAG = "SelectorOptionUtils"

class SendOptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class SendOptionsAdapter(private val c: Activity) : RecyclerView.Adapter<SendOptionHolder>() {

    companion object {

        private val colors = arrayOf(
            R.color.c_doc,
            R.color.c_apps,
            R.color.c_img,
            R.color.c_vid,
            R.color.c_aud,
            R.color.c_com
        )

        private val activities = arrayOf(
            FileType.Documents to RESULT_CODE_INPUT_DOCS,
            FileType.Apps to RESULT_CODE_INPUT_APPS,
            FileType.Images to RESULT_CODE_INPUT_IMAGES,
            FileType.Videos to RESULT_CODE_INPUT_VIDEOS,
            FileType.Audios to RESULT_CODE_INPUT_AUDIOS,
            FileType.Compressed to RESULT_CODE_INPUT_COMPRESSED
        )

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SendOptionHolder =
        SendOptionHolder(
            LayoutInflater.from(c)
                .inflate(R.layout.item_option, parent, false)
        )

    override fun getItemCount(): Int = activities.size

    override fun onBindViewHolder(holder: SendOptionHolder, position: Int) {
        val pair = activities[position]
        val color = c.resources.getColor(colors[position])
        holder.itemView.send_option.background.setTint(color)
        holder.itemView.send_option.text = pair.first.name
        holder.itemView.send_option.setTextColor(color)
        holder.itemView.setOnClickListener {
            Log.d(TAG, "option click: $pair")
            val activity = if (pair.first == FileType.Apps) AppsSelectorActivity::class.java
            else MediaSelectorsActivity::class.java

            c.startActivityForResult(
                Intent(c, activity).putExtra(DATA_FILE_TYPE, pair.first.name),
                pair.second
            )
        }
    }

}