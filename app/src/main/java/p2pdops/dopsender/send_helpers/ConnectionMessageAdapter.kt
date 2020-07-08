package p2pdops.dopsender.send_helpers

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_wconn_handle_end.view.*
import kotlinx.android.synthetic.main.item_wconn_handle_init.view.*
import kotlinx.android.synthetic.main.item_wconn_receive_file.view.*
import kotlinx.android.synthetic.main.item_wconn_send_file.view.*
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.*
import p2pdops.dopsender.utils.docsColormap
import p2pdops.dopsender.utils.dpToPx
import p2pdops.dopsender.utils.humanizeBytes
import java.io.File

open class BaseConnHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ConnInitHolder(itemView: View) : BaseConnHolder(itemView) {
    fun setItem(item: ConnectionItem) {
        itemView.initConnectedInfo.text = item.toString()
    }
}

class ConnEndHolder(itemView: View) : BaseConnHolder(itemView) {
    fun setItem(item: ConnectionItem) {
        itemView.endConnectedInfo.text = item.toString()
    }
}


class ConnSendHolder(itemView: View) : BaseConnHolder(itemView) {
    fun setItem(item: ConnSendFileItem) {
        val itemFile = File(item.filePath)
        when (item.fileType) {
            FileType.COMPRESSED -> {
                itemView.sendFileIcon.setColorFilter(
                    getColorStateList(
                        itemView.context,
                        R.color.c_com
                    ).defaultColor, PorterDuff.Mode.SRC_IN
                )
                itemView.sendFileIcon.setImageDrawable(
                    getDrawable(
                        itemView.context,
                        R.drawable.ic_compressed
                    )
                )
            }
            FileType.DOC -> {
                ContextCompat.getColor(
                    itemView.context!!,
                    docsColormap.getOrElse(itemFile.extension) { R.color.color_pdf })
                    .apply {
                        itemView.sendFileIcon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
                        itemView.sendFileIcon.setImageDrawable(
                            getDrawable(
                                itemView.context,
                                R.drawable.ic_doc_file
                            )
                        )
                    }
            }

            FileType.AUDIO -> {
                itemView.sendFileIcon.setColorFilter(
                    getColorStateList(
                        itemView.context,
                        R.color.c_aud
                    ).defaultColor, PorterDuff.Mode.SRC_IN
                )
                itemView.sendFileIcon.setImageDrawable(
                    getDrawable(
                        itemView.context,
                        R.drawable.ic_audio_outline
                    )
                )
            }
            FileType.IMAGE -> {
                Glide.with(itemView).load(item.filePath).thumbnail(0.2f).centerCrop()
                    .error(R.drawable.ic_images).into(itemView.sendFileIcon)
            }
            FileType.VIDEO -> Glide.with(itemView)
                .load(item.filePath).thumbnail(0.2f).centerCrop()
                .error(R.drawable.ic_images).into(itemView.sendFileIcon)
            FileType.APK -> Glide.with(itemView).load(item.filePath).thumbnail(0.2f)
                .centerCrop()
                .into(itemView.sendFileIcon)
        }

        itemView.sendFileName.text = item.fileName
        itemView.sendFileName.isSelected = true
        itemView.sendFileType.text = item.fileType.name
        itemView.sendFileSize.text = humanizeBytes(itemFile.length())
        itemView.sendFileStatus.text = when (item.status) {
            ConnFileStatusTypes.WAITING -> "In queue"
            ConnFileStatusTypes.LOADING -> "Sending"
            ConnFileStatusTypes.LOADED -> "Sent"
        }
    }
}

class ConnReceiveHolder(itemView: View) : BaseConnHolder(itemView) {
    fun setItem(item: ConnReceiveFileItem) {

        val itemFile = File(item.filePath)

        when (item.fileType) {
            FileType.COMPRESSED -> {
                itemView.receiveFileIcon.setColorFilter(
                    getColorStateList(
                        itemView.context,
                        R.color.c_com
                    ).defaultColor, PorterDuff.Mode.SRC_IN
                )
                itemView.receiveFileIcon.setImageDrawable(
                    getDrawable(
                        itemView.context,
                        R.drawable.ic_compressed
                    )
                )
            }
            FileType.DOC -> {
                ContextCompat.getColor(
                    itemView.context!!,
                    docsColormap.getOrElse(itemFile.extension) { R.color.color_pdf })
                    .apply {
                        itemView.receiveFileIcon.setColorFilter(this, PorterDuff.Mode.SRC_IN)
                        itemView.receiveFileIcon.setImageDrawable(
                            getDrawable(
                                itemView.context,
                                R.drawable.ic_doc_file
                            )
                        )
                    }
            }

            FileType.AUDIO -> {
                itemView.receiveFileIcon.setColorFilter(
                    getColorStateList(
                        itemView.context,
                        R.color.c_aud
                    ).defaultColor, PorterDuff.Mode.SRC_IN
                )
                itemView.receiveFileIcon.setImageDrawable(
                    getDrawable(
                        itemView.context,
                        R.drawable.ic_audio_outline
                    )
                )
            }
            FileType.IMAGE -> Glide.with(itemView)
                .load(item.filePath).thumbnail(0.2f).centerCrop()
                .error(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_images)
                )
                .into(itemView.receiveFileIcon)
            FileType.VIDEO -> Glide.with(itemView)
                .load(item.filePath).thumbnail(0.2f).centerCrop().error(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_video)
                )
                .into(itemView.receiveFileIcon)

            FileType.APK -> {

                if (item.status == ConnFileStatusTypes.LOADED) {
                    val pm = itemView.context.packageManager
                    pm?.getPackageArchiveInfo(
                        item.filePath,
                        PackageManager.GET_META_DATA
                    )?.let {
                        Glide
                            .with(itemView)
                            .load(it.applicationInfo.loadIcon(pm))
                            .error(R.drawable.ic_apk)
                            .thumbnail(0.2f)
                            .centerCrop()
                            .into(itemView.receiveFileIcon)
                    }
                } else
                    Glide
                        .with(itemView)
                        .load(R.drawable.ic_apk)
                        .thumbnail(0.2f)
                        .centerCrop()
                        .into(itemView.receiveFileIcon)

            }
        }

        itemView.receiveFileName.text = item.fileName
        itemView.receiveFileName.isSelected = true
        itemView.receiveFileType.text = item.fileType.name
        itemView.receiveFileSize.text = humanizeBytes(item.fileSize)

        itemView.receiveFileStatus.text = when (item.status) {
            ConnFileStatusTypes.WAITING -> "In queue"
            ConnFileStatusTypes.LOADING -> "Receiving"
            ConnFileStatusTypes.LOADED -> "Received"
        }
    }
}

class ConnectionMessageAdapter(
    private val list: ArrayList<ConnectionItem>,
    private val activity: Activity
) :
    RecyclerView.Adapter<BaseConnHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseConnHolder {
        return when (viewType) {
            // connection
            ConnectionMessageType.INIT.ordinal -> ConnInitHolder(
                LayoutInflater.from(activity)
                    .inflate(R.layout.item_wconn_handle_init, parent, false)
            )
            ConnectionMessageType.END.ordinal -> ConnEndHolder(
                LayoutInflater.from(activity).inflate(R.layout.item_wconn_handle_end, parent, false)
            )

            ConnectionMessageType.SEND_FILE.ordinal -> ConnSendHolder(
                LayoutInflater.from(activity)
                    .inflate(R.layout.item_wconn_send_file, parent, false)
            )

            ConnectionMessageType.RECEIVE_FILE.ordinal -> ConnReceiveHolder(
                LayoutInflater.from(activity)
                    .inflate(R.layout.item_wconn_receive_file, parent, false)
            )

            else -> BaseConnHolder(View(activity))
        }
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: BaseConnHolder, position: Int) {
        when (list[position].type) {
            ConnectionMessageType.INIT -> (holder as ConnInitHolder).setItem(list[position] as ConnStatus)
            ConnectionMessageType.END -> (holder as ConnEndHolder).setItem(list[position] as ConnStatus)
            ConnectionMessageType.SEND_FILE -> {
                (holder as ConnSendHolder).setItem(list[position] as ConnSendFileItem)

                if (position > 0 && (list[position - 1] is ConnReceiveFileItem || list[position - 1] is ConnStatus)) {
                    holder.itemView.updatePadding(top = dpToPx(20))//layoutParams = marginParams;
                }

            }
            ConnectionMessageType.RECEIVE_FILE -> {
                (holder as ConnReceiveHolder).setItem(list[position] as ConnReceiveFileItem)

                if (position > 0 && (list[position - 1] is ConnSendFileItem || list[position - 1] is ConnStatus)) {
                    holder.itemView.updatePadding(top = dpToPx(20))//layoutParams = marginParams;
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = list[position].type.ordinal

}