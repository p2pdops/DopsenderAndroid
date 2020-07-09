package p2pdops.dopsender.send_helpers

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import p2pdops.dopsender.*

import p2pdops.dopsender.adapters.WifiDevicesAdapter
import p2pdops.dopsender.utils.*


fun Activity.openSheet(view: View, viewToClose: View? = null) {
    if (viewToClose != null)
        closeSheet(viewToClose)

    BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_HALF_EXPANDED
    BottomSheetBehavior.from(view).isHideable = false
}

fun Activity.closeSheet(view: View) {
    BottomSheetBehavior.from(view).isHideable = true
    BottomSheetBehavior.from(view).state = BottomSheetBehavior.STATE_HIDDEN
}

fun Activity.loadDevicesList(recycler: RecyclerView, adapter: WifiDevicesAdapter) {
    recycler.setHasFixedSize(true)
    recycler.layoutManager = LinearLayoutManager(recycler.context)
    recycler.adapter = adapter
}

fun Activity.setupSendOptions(optionsRecyclerView: RecyclerView) {
    optionsRecyclerView.setHasFixedSize(true)
    optionsRecyclerView.layoutManager =
        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    optionsRecyclerView.adapter = SendOptionsAdapter(this)
}

class SendOptionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView.findViewById<TextView>(R.id.send_option)
}

class SendOptionsAdapter(private val c: Activity) : RecyclerView.Adapter<SendOptionHolder>() {

    companion object {
        private const val TAG = "SendOptionsUtils"
        private val colors = arrayOf(
            R.color.c_doc,
            R.color.c_apps,
            R.color.c_img,
            R.color.c_vid,
            R.color.c_aud,
            R.color.c_com
        )

        private val maps = arrayOf(
            "Documents", "Apps",
            "Images",
            "Videos",
            "Audios",
            "Compressed"
        )

        private val activities = arrayOf(
            DocsSelectorActivity::class.java to RESULT_CODE_INPUT_DOCS,
            AppsSelectorActivity::class.java to RESULT_CODE_INPUT_APPS,
            ImagesSelectorActivity::class.java to RESULT_CODE_INPUT_IMAGES,
            VideosSelectorActivity::class.java to RESULT_CODE_INPUT_VIDEOS,
            AudiosSelectorActivity::class.java to RESULT_CODE_INPUT_AUDIOS,
            CompressedSelectorActivity::class.java to RESULT_CODE_INPUT_COMPRESSED
        )

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SendOptionHolder =
        SendOptionHolder(
            LayoutInflater.from(c)
                .inflate(R.layout.item_option, parent, false)
        )

    override fun getItemCount(): Int = maps.size

    override fun onBindViewHolder(holder: SendOptionHolder, position: Int) {
        holder.textView.text = maps[position]
        val color = c.resources.getColor(colors[position])
        holder.textView.setTextColor(color)
        holder.textView.background.setTint(color)
        holder.itemView.setOnClickListener {
            Log.d(TAG, "option click: ")
            c.startActivityForResult(
                Intent(c, activities[position].first),
                activities[position].second
            )
        }
    }

}

